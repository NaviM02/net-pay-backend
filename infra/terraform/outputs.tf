output "instance_id" { value = aws_instance.backend.id }
output "public_ip" { value = aws_eip.backend.public_ip }
output "https_url" { value = "https://${aws_eip.backend.public_ip}" }
output "ecr_repository_url" { value = aws_ecr_repository.backend.repository_url }
output "github_actions_role_arn" { value = aws_iam_role.github_actions.arn }
output "ssm_deploy_document" { value = aws_ssm_document.deploy.name }
output "runtime_parameter_name" { value = var.runtime_parameter_name }
output "github_oidc_provider_arn" { value = local.github_oidc_arn }
