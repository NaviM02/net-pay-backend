variable "aws_region" {
  type        = string
  description = "Choose after checking the institutional account, availability and pricing."
}
variable "project_name" {
  type    = string
  default = "netpay"
  validation {
    condition     = can(regex("^[a-z][a-z0-9-]{2,20}$", var.project_name))
    error_message = "Use 3-21 lowercase letters, digits or hyphens."
  }
}
variable "github_owner" {
  type        = string
  description = "GitHub user/org that owns the backend repository."
  validation {
    condition     = can(regex("^[A-Za-z0-9-]+$", var.github_owner))
    error_message = "Enter the GitHub owner name."
  }
}
variable "github_repository" {
  type        = string
  description = "Backend repository name (without owner)."
  validation {
    condition     = can(regex("^[A-Za-z0-9_.-]+$", var.github_repository))
    error_message = "Enter only the GitHub repository name."
  }
}
variable "existing_github_oidc_provider_arn" {
  type        = string
  default     = null
  description = "Set this if token.actions.githubusercontent.com provider already exists in this AWS account."
}
variable "acme_email" {
  type        = string
  description = "Contact email used by Caddy/Let's Encrypt."
  validation {
    condition     = can(regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", var.acme_email))
    error_message = "Enter a valid email address."
  }
}
variable "runtime_parameter_name" {
  type        = string
  default     = "/netpay/prod/runtime"
  description = "Create this SSM SecureString separately; never put its value in Terraform."
  validation {
    condition     = can(regex("^/[A-Za-z0-9_.-]+(/[A-Za-z0-9_.-]+)*$", var.runtime_parameter_name))
    error_message = "Use a valid SSM parameter path."
  }
}
variable "instance_type" {
  type        = string
  default     = "t3.small"
  description = "t3.small is a low-cost candidate; choose after checking price and memory needs."
  validation {
    condition     = contains(["t3.small", "t3.medium", "t3a.small", "t3a.medium"], var.instance_type)
    error_message = "Supported candidates: t3/t3a small or medium (x86_64)."
  }
}
variable "root_volume_gb" {
  type    = number
  default = 24
  validation {
    condition     = var.root_volume_gb >= 20 && var.root_volume_gb <= 200
    error_message = "Choose 20-200 GiB based on database, logs and container image growth."
  }
}
variable "vpc_cidr" {
  type    = string
  default = "10.40.0.0/16"
}
variable "subnet_cidr" {
  type    = string
  default = "10.40.1.0/24"
}
