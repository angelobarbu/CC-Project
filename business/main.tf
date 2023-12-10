## Define the provider (in this case, using the default provider for Docker)
#terraform {
# required_providers {
#   docker = {
#     source = "kreuzwerker/docker"
#     version = "~> 2.13.0"
#   }
# }
#}
#
#resource "docker_network" "app_network" {
#  name = "app_network"
#}
#
#provider "docker" {
#  version = "~> 2.6"
#  host    = "npipe:////.//pipe//docker_engine"
#}
#
## Define the Spring Boot application container
#resource "docker_container" "appBusiness" {
#  name  = "appBusiness"
#  image = "business:latest"
#
#  ports {
#    internal = 54321
#    external = 8080
#  }
#
#  network_mode = docker_network.app_network.name
#  env = [
#    "SPRING_DATASOURCE_URL=jdbc:postgresql://appdb:5432/postgres_db",
#    "SPRING_DATASOURCE_USERNAME=user",
#    "SPRING_DATASOURCE_PASSWORD=password",
#    "SPRING_JPA_HIBERNATE_DDL_AUTO=update",
#  ]
#}
#
## Define the PostgreSQL container
#resource "docker_container" "appdb" {
#  name  = "appdb"
#  image = "postgres:latest"
#  network_mode = docker_network.app_network.name
#  env = [
#    "POSTGRES_USER=user",
#    "POSTGRES_PASSWORD=password",
#    "POSTGRES_DB=postgres_db",
#  ]
#}

terraform {
  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.0"
    }
  }
}

provider "kubernetes" {
  config_path = "C:/Users/anghe/.kube/config"
}

resource "kubernetes_namespace" "app_namespace" {
  metadata {
    name = "app-namespace"
  }
}

resource "kubernetes_deployment" "appBusiness" {
  metadata {
    name      = "app-business"
    namespace = kubernetes_namespace.app_namespace.metadata[0].name
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "app-business"
      }
    }

    template {
      metadata {
        labels = {
          app = "app-business"
        }
      }

      spec {
        container {
          name  = "app-business"
          image = "business:latest"

          port {
            container_port = 54321
          }

          env {
            name  = "SPRING_DATASOURCE_URL"
            value = "jdbc:postgresql://appdb:5432/postgres_db"
          }

          env {
            name  = "SPRING_DATASOURCE_USERNAME"
            value = "user"
          }

          env {
            name  = "SPRING_DATASOURCE_PASSWORD"
            value = "password"
          }

          env {
            name  = "SPRING_JPA_HIBERNATE_DDL_AUTO"
            value = "update"
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "appBusiness_service" {
  metadata {
    name      = "app-business-service"
    namespace = kubernetes_namespace.app_namespace.metadata[0].name
  }

  spec {
    selector = {
      app = "app-business"
    }

    port {
      port        = 8080
      target_port = 54321
    }

    type = "LoadBalancer"
  }
}

resource "kubernetes_deployment" "appdb" {
  metadata {
    name      = "appdb"
    namespace = kubernetes_namespace.app_namespace.metadata[0].name
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "appdb"
      }
    }

    template {
      metadata {
        labels = {
          app = "appdb"
        }
      }

      spec {
        container {
          name  = "appdb"
          image = "postgres:latest"

          env {
            name  = "POSTGRES_USER"
            value = "user"
          }

          env {
            name  = "POSTGRES_PASSWORD"
            value = "password"
          }

          env {
            name  = "POSTGRES_DB"
            value = "postgres_db"
          }
        }
      }
    }
  }
}