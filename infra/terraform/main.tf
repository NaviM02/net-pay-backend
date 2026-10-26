data "aws_caller_identity" "current" {}
data "aws_partition" "current" {}
data "aws_availability_zones" "available" {
  state = "available"
}
data "aws_ssm_parameter" "amazon_linux_2023" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64"
}

locals {
  account_id     = data.aws_caller_identity.current.account_id
  partition      = data.aws_partition.current.partition
  repo_name      = "${var.project_name}-backend"
  repo_arn       = aws_ecr_repository.backend.arn
  parameter_arn  = "arn:${local.partition}:ssm:${var.aws_region}:${local.account_id}:parameter${var.runtime_parameter_name}"
  instance_arn   = "arn:${local.partition}:ec2:${var.aws_region}:${local.account_id}:instance/${aws_instance.backend.id}"
  deploy_doc_arn = "arn:${local.partition}:ssm:${var.aws_region}:${local.account_id}:document/${aws_ssm_document.deploy.name}"
}

resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags                 = { Name = "${var.project_name}-vpc" }
}
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id
  tags   = { Name = "${var.project_name}-igw" }
}
resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.subnet_cidr
  map_public_ip_on_launch = true
  availability_zone       = data.aws_availability_zones.available.names[0]
  tags                    = { Name = "${var.project_name}-public" }
}
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }
  tags = { Name = "${var.project_name}-public" }
}
resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

resource "aws_security_group" "backend" {
  name        = "${var.project_name}-backend"
  description = "Public HTTPS proxy only; no SSH, application or PostgreSQL ingress."
  vpc_id      = aws_vpc.main.id
  ingress {
    description = "HTTP for ACME challenge and HTTPS redirect"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    description = "HTTPS API"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    description = "Outbound package, ECR, SSM, SSM Parameter Store and API integrations"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = { Name = "${var.project_name}-backend" }
}

resource "aws_ecr_repository" "backend" {
  name                 = local.repo_name
  image_tag_mutability = "IMMUTABLE"
  image_scanning_configuration { scan_on_push = true }
  encryption_configuration { encryption_type = "AES256" }
}
resource "aws_ecr_lifecycle_policy" "backend" {
  repository = aws_ecr_repository.backend.name
  policy = jsonencode({
    rules = [{
      rulePriority = 1
      description  = "Keep the latest 12 immutable deploy candidates"
      selection    = { tagStatus = "any", countType = "imageCountMoreThan", countNumber = 12 }
      action       = { type = "expire" }
    }]
  })
}

resource "aws_iam_role" "instance" {
  name = "${var.project_name}-ec2"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow", Principal = { Service = "ec2.amazonaws.com" }, Action = "sts:AssumeRole"
    }]
  })
}
resource "aws_iam_role_policy_attachment" "ssm_core" {
  role       = aws_iam_role.instance.name
  policy_arn = "arn:${local.partition}:iam::aws:policy/AmazonSSMManagedInstanceCore"
}
resource "aws_iam_role_policy" "instance" {
  name = "${var.project_name}-runtime"
  role = aws_iam_role.instance.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      { Effect = "Allow", Action = ["ssm:GetParameter"], Resource = local.parameter_arn },
      { Effect = "Allow", Action = ["kms:Decrypt"], Resource = "arn:${local.partition}:kms:${var.aws_region}:${local.account_id}:key/*", Condition = { StringEquals = { "kms:ViaService" = "ssm.${var.aws_region}.amazonaws.com" } } },
      { Effect = "Allow", Action = ["ecr:GetAuthorizationToken"], Resource = "*" },
      { Effect = "Allow", Action = ["ecr:BatchCheckLayerAvailability", "ecr:GetDownloadUrlForLayer", "ecr:BatchGetImage"], Resource = local.repo_arn }
    ]
  })
}
resource "aws_iam_instance_profile" "backend" {
  name = "${var.project_name}-ec2"
  role = aws_iam_role.instance.name
}

resource "aws_instance" "backend" {
  ami                         = data.aws_ssm_parameter.amazon_linux_2023.value
  instance_type               = var.instance_type
  subnet_id                   = aws_subnet.public.id
  vpc_security_group_ids      = [aws_security_group.backend.id]
  associate_public_ip_address = true
  iam_instance_profile        = aws_iam_instance_profile.backend.name
  user_data = templatefile("${path.module}/user-data.sh.tftpl", {
    region          = var.aws_region
    ecr_repository  = aws_ecr_repository.backend.repository_url
    parameter_name  = var.runtime_parameter_name
    api_host        = aws_eip.backend.public_ip
    acme_email      = var.acme_email
    compose_content = file("${path.module}/../../compose.prod.yml")
    caddy_content   = file("${path.module}/../../deploy/Caddyfile")
    start_content   = file("${path.module}/../../deploy/instance-start.sh")
    release_content = file("${path.module}/../../deploy/release.sh")
    env_writer      = file("${path.module}/../../deploy/write-runtime-env.py")
  })
  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 1
  }
  root_block_device {
    volume_type           = "gp3"
    volume_size           = var.root_volume_gb
    encrypted             = true
    delete_on_termination = false
  }
  tags = { Name = "${var.project_name}-backend" }
}
resource "aws_eip" "backend" {
  domain = "vpc"
  tags   = { Name = "${var.project_name}-backend" }
}
resource "aws_eip_association" "backend" {
  allocation_id = aws_eip.backend.id
  instance_id   = aws_instance.backend.id
}

resource "aws_ssm_document" "deploy" {
  name          = "${var.project_name}-deploy"
  document_type = "Command"
  content = jsonencode({
    schemaVersion = "2.2"
    description   = "Deploy an immutable Git commit image to the NetPay EC2 instance."
    parameters = {
      Release = {
        type              = "String"
        allowedPattern    = "^[a-f0-9]{40}$"
        interpolationType = "ENV_VAR"
      }
    }
    mainSteps = [{
      action = "aws:runShellScript"
      name   = "deploy"
      inputs = {
        timeoutSeconds = "1200"
        runCommand     = ["set -eu", "sudo /usr/local/bin/netpay-release \"$SSM_Release\""]
      }
    }]
  })
}

resource "aws_iam_user" "github_actions" {
  name = "${var.project_name}-github-actions"
  path = "/"
}

resource "aws_iam_user_policy" "github_actions" {
  name = "${var.project_name}-deploy"
  user = aws_iam_user.github_actions.name
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      { Effect = "Allow", Action = ["ecr:GetAuthorizationToken"], Resource = "*" },
      { Effect = "Allow", Action = ["ecr:BatchCheckLayerAvailability", "ecr:CompleteLayerUpload", "ecr:InitiateLayerUpload", "ecr:PutImage", "ecr:UploadLayerPart"], Resource = local.repo_arn },
      { Effect = "Allow", Action = ["ssm:SendCommand"], Resource = [local.deploy_doc_arn, local.instance_arn] },
      { Effect = "Allow", Action = ["ssm:GetCommandInvocation"], Resource = "*" }
    ]
  })
}
