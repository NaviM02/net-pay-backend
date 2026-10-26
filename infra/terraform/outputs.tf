output "instance_id" { value = aws_instance.backend.id }
output "public_ip" { value = aws_eip.backend.public_ip }
output "https_url" { value = "https://${aws_eip.backend.public_ip}" }
output "ecr_repository_url" { value = aws_ecr_repository.backend.repository_url }
output "github_actions_user_name" { value = aws_iam_user.github_actions.name }
output "ssm_deploy_document" { value = aws_ssm_document.deploy.name }
output "runtime_parameter_name" { value = var.runtime_parameter_name }
