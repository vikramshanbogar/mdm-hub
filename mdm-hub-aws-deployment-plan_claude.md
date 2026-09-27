# mdm-hub → AWS deployment plan (Terraform)

Goal: quick deploy for experimentation, structured so it also doubles as
an AWS services revision exercise.

Source: https://github.com/vikramshanbogar/mdm-hub
Image: `vikramsvk1/mdm-hub:latest` (Docker Hub)

App notes relevant to deployment:
- Spring Boot 3.5 / JDK 21, listens on port 8080 (overridable via `SERVER_PORT`)
- DB config via env vars: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- Health check endpoint: `/actuator/health`

## Why ECS Fargate + RDS

No servers to patch, deploys in minutes, and along the way it touches VPC
networking, IAM, load balancing, container orchestration, managed
databases, and logging — the core AWS vocabulary.

Alternatives considered:
- **Single EC2 + docker-compose** — fastest to write, but teaches very
  little new.
- **EKS** — teaches more, but slow and fiddly to stand up just to
  experiment.

## Architecture

- One VPC, public + private subnets across 2 AZs
- Public subnet: Application Load Balancer (internet-facing)
- Private subnet: ECS Fargate service (the app) and RDS PostgreSQL
- ALB → ECS Fargate task (port 8080) → RDS (port 5432)
- Container image pulled directly from Docker Hub (no ECR needed yet)

## Terraform module plan (build in this order)

1. **Networking** — VPC, 2 public + 2 private subnets, internet gateway,
   NAT gateway (optional — see cost note below).
2. **Security groups** — ALB SG (80/443 from internet), ECS task SG
   (8080 from ALB only), RDS SG (5432 from ECS SG only).
3. **RDS** — single-AZ `db.t4g.micro` Postgres in the private subnets,
   `publicly_accessible = false`. Generate the password with
   `random_password` and store it in Secrets Manager rather than a
   plaintext `.tfvars`.
4. **IAM** — ECS task execution role (pulls image, writes logs, reads
   the DB secret) and a separate task role.
5. **CloudWatch log group** — for container stdout/stderr.
6. **ECS cluster + task definition + service** — task definition
   references `vikramsvk1/mdm-hub:latest` directly, maps port 8080,
   injects DB env vars from the secret via `secrets` (not
   `environment`), `desired_count = 1`.
7. **ALB** — internet-facing, target group with health check path
   `/actuator/health`, listener on port 80 forwarding to the target
   group.
8. **Outputs** — ALB DNS name, so you can `curl` it right after
   `terraform apply`.

## Suggested file layout

```
terraform/
  providers.tf       # aws provider, region, backend
  variables.tf        # region, image tag, db_instance_class, app_count, etc.
  network.tf           # vpc, subnets, igw, nat, route tables
  security_groups.tf
  rds.tf
  secrets.tf
  iam.tf
  ecs.tf               # cluster, task def, service
  alb.tf
  outputs.tf
```

## Decisions worth making upfront

- **State**: start with local state for a solo experiment; add an S3
  backend later as its own learning exercise.
- **NAT vs public Fargate task**: to keep costs near-zero, skip the NAT
  gateway (~$32/mo) and put the ECS task in a public subnet with
  `assign_public_ip = true` (task SG still only open to the ALB). Add
  the NAT gateway back later to practice private-subnet egress
  patterns properly.
- **RDS vs containerized Postgres**: running Postgres as a second
  Fargate task is cheaper and skips RDS entirely, but loses the
  RDS-specific learning (parameter groups, snapshots, Multi-AZ). Worth
  doing RDS at least once.
- **Teardown discipline**: get `terraform destroy` working cleanly from
  day one — RDS, NAT gateway, and ALB left running are what quietly
  rack up cost.

## Natural next step

Once ready to write code: networking + security groups first (so you
can `apply` and see the VPC come up), then RDS, then ECS + ALB.

Future revision ideas (from the repo's own README):
- Push the image to ECR instead of pulling from Docker Hub
- Move from ECS Fargate to EKS
- Add Flyway/Liquibase migrations instead of `ddl-auto: update`
- Add Spring Security + method-level auth on the merge endpoint
