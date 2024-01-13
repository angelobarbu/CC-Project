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

resource "kubernetes_namespace" "product-store" {
  metadata {
    name = "product-store"
  }
}

resource "kubernetes_deployment" "store-app" {
  metadata {
    name      = "store-app"
    namespace = kubernetes_namespace.product-store.metadata[0].name
  }
  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "store-app"
      }
    }

    template {
      metadata {
        labels = {
          app = "store-app"
        }
      }

      spec {
        container {
          image = "2y6cx0/business:latest"
          name  = "store-app"
          port {
            container_port = 54321
          }

          env {
            name  = "SPRING_DATASOURCE_URL"
            value = "jdbc:postgresql://postgresql-db-service:30208/postgres_db"
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

          env {
            name  = "SPRING_AUTH_SERVICE_URL"
            value = "http://auth-app-service:30201/protected"
          }

        }
      }
    }
  }
}

resource "kubernetes_service" "store-app-service" {

  metadata {
    name = "store-app-service"
    namespace =  kubernetes_namespace.product-store.metadata[0].name
  }

  spec {
    type = "NodePort"

    selector = {
        app = "store-app"
    }

    port {
      port        = 54321
      target_port = 54321
      node_port   = 30222
    }
  }
}

resource "kubernetes_deployment" "postgresql-db" {
  metadata {
    name      = "postgresql-db"
    namespace = kubernetes_namespace.product-store.metadata[0].name
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "postgresql-db"
      }
    }

    template {
      metadata {
        labels = {
          app = "postgresql-db"
        }
      }

      spec {
        container {
          name  = "postgresql-db"
          image = "postgres:latest"

#          volume_mount {
#            mount_path     = "/var/lib/postgresql/data"
#            name           = "postgres-data"
#          }

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

#        volume {
#          name = "postgresql-data"
#
#          persistent_volume_claim {
#            claim_name = kubernetes_persistent_volume_claim.postgres-pvc.metadata[0].name
#          }
#        }
      }
    }
  }
}

resource "kubernetes_service" "postgresql-db-service" {

  metadata {
    name = "postgresql-db-service"
    namespace =  kubernetes_namespace.product-store.metadata[0].name
  }

  spec {
    selector = {
      app = "postgresql-db"
    }

    port {
      port        = 30208
      target_port = 5432
    }
  }
}

resource "kubernetes_deployment" "auth_app" {
  metadata {
    name = "auth-app"
    namespace = kubernetes_namespace.product-store.metadata[0].name
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "auth-app"
      }
    }

    template {
      metadata {
        labels = {
          app = "auth-app"
        }
      }

      spec {
        container {
          image = "2y6cx0/auth-app:latest"
          name  = "auth-app"
          port {
            container_port = 5000
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "auth-app-service" {
  metadata {
    name = "auth-app-service"
    namespace = kubernetes_namespace.product-store.metadata[0].name
  }

  spec {
    selector = {
      app = "auth-app"
    }

    type = "NodePort"
    port {
      node_port   = 30201
      port        = 30201
      target_port = 5000
    }
  }
}

resource "kubernetes_deployment" "dbeaver" {
  metadata {
    name      = "dbeaver"
    namespace = kubernetes_namespace.product-store.metadata[0].name
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "dbeaver"
      }
    }

    template {
      metadata {
        labels = {
          app = "dbeaver"
        }
      }

      spec {
        container {
          name  = "dbeaver"
          image = "dbeaver/cloudbeaver:latest"

#          env {
#            name  = "POSTGRES_USER"
#            value = "user"
#          }
#
#          env {
#            name  = "POSTGRES_PASSWORD"
#            value = "password"
#          }
#
#          env {
#            name  = "POSTGRES_DB"
#            value = "postgres_db"
#          }
        }
      }
    }
  }
}

resource "kubernetes_service" "dbeaver-service" {

  metadata {
    name = "dbeaver-service"
    namespace =  kubernetes_namespace.product-store.metadata[0].name
  }

  spec {
    selector = {
      app = "dbeaver"
    }

    type = "NodePort"

    port {
      port        = 30209
      node_port   = 30209
      target_port = 8978
    }
  }
}

resource "kubernetes_deployment" "portainer" {
  metadata {
    name      = "portainer"
    namespace = kubernetes_namespace.product-store.metadata[0].name
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "portainer"
      }
    }

    template {
      metadata {
        labels = {
          app = "portainer"
        }
      }

      spec {
        container {
          name  = "portainer"
          image = "portainer/portainer-ce:latest"
        }
      }
    }
  }
}

resource "kubernetes_service" "portainer-service" {

  metadata {
    name = "portainer-service"
    namespace =  kubernetes_namespace.product-store.metadata[0].name
  }

  spec {
    selector = {
      app = "portainer"
    }

    type = "LoadBalancer"

    port {
      port        = 30210
      node_port   = 30210
      target_port = 9000
    }
  }
}
















#resource "kubernetes_persistent_volume" "postgres-pv" {
#  metadata {
#    name      = "postgres-pv"
#  }
#
#  spec {
#    access_modes = ["ReadWriteOnce"]
#    capacity     = {
#      storage = "2Gi"
#    }
#
#    persistent_volume_source {
#      host_path {
#        path = "C:/Users/anghe/OneDrive/Desktop/Master-1/Cloud/data"
#      }
#    }
#  }
#}
#
#resource "kubernetes_persistent_volume_claim" "postgres-pvc" {
#  metadata {
#    name      = "postgres-pvc"
##    namespace = kubernetes_namespace.product-store.metadata[0].name
#  }
#
#  spec {
#    access_modes = ["ReadWriteOnce"]
#    volume_name = kubernetes_persistent_volume.postgres-pv.metadata[0].name
#
#    resources {
#      requests = {
#        storage = "1Gi"
#      }
#    }
#  }
#}