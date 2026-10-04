# Kubernetes Деплоймент проєкту Aogiri (Лабораторна робота №3)

Дана директорія містить повний набір декларативних маніфестів Kubernetes для розгортання мікросервісу `aogiri-user` (Java 21 / Spring Boot 3) та бази даних PostgreSQL у локальному кластері Kubernetes (Minikube).

---

## 1. Архітектура розгортання

Розгортання реалізовано в ізольованому просторі імен `aogiri` з розділенням на рівень зберігання даних (Stateful) та рівень обробки запитів (Stateless):

```
                                      [ Клієнт / Браузер ]
                                               │
                         ┌─────────────────────┴─────────────────────┐
                         │                                           │
                         ▼                                           ▼
                 [ Ingress Controller ]                     [ NodePort Service ]
                 (aogiri-ingress: /)                        (user-service: 30081)
                         │                                           │
                         └─────────────────────┬─────────────────────┘
                                               │
                                               ▼
                              ┌──────────────────────────────────┐
                              │  user-deployment (2+ репліки)    │
                              │  - Liveness / Readiness probes   │
                              │  - Resource Requests & Limits    │
                              │  - ConfigMap & Secret Injected   │
                              └────────────────┬─────────────────┘
                                               │
                                               │ JDBC (postgres-service:5432)
                                               ▼
                              ┌──────────────────────────────────┐
                              │     postgres-service (5432)      │
                              └────────────────┬─────────────────┘
                                               │
                                               ▼
                              ┌──────────────────────────────────┐
                              │       postgres-deployment        │
                              └────────────────┬─────────────────┘
                                               │
                                               ▼
                              ┌──────────────────────────────────┐
                              │   postgres-pvc (1Gi Storage)     │
                              └──────────────────────────────────┘
```

### Основні компоненти:
1. **База даних (Stateful)**:
   - `postgres-pvc`: PersistentVolumeClaim на 1Gi з режимом `ReadWriteOnce`. Гарантує збереження даних користувачів навіть при повному перестворенні чи падінні Pod'а PostgreSQL.
   - `postgres-deployment`: Запуск офіційного контейнера `postgres:16-alpine`.
   - `postgres-service`: Внутрішній сервіс типу `ClusterIP`, що надає стабільне доменне ім'я `postgres-service:5432` для бекенду всередині кластера.
2. **Мікросервіс (Stateless)**:
   - `user-deployment`: Запуск мікросервісу `aogiri-user` (Spring Boot 3 / Java 21) з мінімум 2 репліками.
     - Налаштовано **Resource Requests** (`cpu: 100m`, `memory: 256Mi`) та **Resource Limits** (`cpu: 500m`, `memory: 512Mi`).
     - Налаштовано **Liveness Probe** (`/actuator/health/liveness`) для автоматичного перезапуску при зависанні.
     - Налаштовано **Readiness Probe** (`/actuator/health/readiness`) для виключення поду з балансування трафіку до завершення його прогріву та ініціалізації.
   - `user-service`: Сервіс типу `NodePort` (порт `30081`), що прокидає запити до порти контейнера `8081`.
3. **Маршрутизація (Ingress)**:
   - `aogiri-ingress`: Ingress-ресурс на базі контролера NGINX для маршрутизації HTTP-трафіку на сервіс `user-service`.

---

## 2. Структура файлів

Файли пронумеровані відповідно до логічного порядку їх створення та залежностей:

| Файл | Вид ресурсу (Kind) | Назва ресурсу | Опис призначення |
| :--- | :--- | :--- | :--- |
| `00-namespace.yaml` | `Namespace` | `aogiri` | Ізольований простір імен для уникнення колізій з іншими застосунками |
| `01-postgres-pvc.yaml` | `PersistentVolumeClaim` | `postgres-pvc` | Запит на персистентний диск 1Gi для збереження даних PostgreSQL |
| `02-configmap.yaml` | `ConfigMap` | `user-config` | Неконфіденційні параметри конфігурації середовища |
| `03-secret.yaml` | `Secret` | `user-secret` | Конфіденційні дані (паролі БД, JWT секретний ключ) |
| `04-postgres-deployment.yaml` | `Deployment` | `postgres-deployment` | Опис поду PostgreSQL 16 з монтуванням PVC у `/var/lib/postgresql/data` |
| `05-postgres-service.yaml` | `Service` | `postgres-service` | Внутрішньокластерна точка доступу (ClusterIP) до БД на порту 5432 |
| `06-user-deployment.yaml` | `Deployment` | `user-deployment` | Безстатусний деплоймент мікросервісу (2 репліки, probes, ресурси) |
| `07-user-service.yaml` | `Service` | `user-service` | Зовнішній доступ до мікросервісу через порт вузла `NodePort: 30081` |
| `08-ingress.yaml` | `Ingress` | `aogiri-ingress` | Маршрутизація трафіку через NGINX Ingress Controller |

---

## 3. Опис параметрів конфігурації (ConfigMap та Secret)

### ConfigMap (`user-config`):
Використовується для зберігання параметрів, які не є секретними, але відрізняються між середовищами розгортання:
- `SERVER_PORT: "8081"` — порт, на якому Spring Boot запускає вбудований веб-сервер Tomcat.
- `SPRING_APPLICATION_NAME: "user-service"` — ідентифікатор мікросервісу в системі Aogiri.
- `SPRING_DATASOURCE_URL: "jdbc:postgresql://postgres-service:5432/user_db"` — JDBC-рядок підключення до БД з використанням внутрішнього DNS Kubernetes (`postgres-service`).
- `SPRING_JPA_HIBERNATE_DDL_AUTO: "update"` — автооновлення схеми БД при старті сервісу.
- `SPRING_JPA_SHOW_SQL: "true"` — логування SQL-запитів для діагностики.
- `APP_ENV: "kubernetes-production"` — маркер середовища запуску.
- `MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED: "true"` — активація стандартних Kubernetes probe endpoints у Spring Boot Actuator.
- `MANAGEMENT_HEALTH_LIVENESSSTATE_ENABLED: "true"` — публікація перевірки `/actuator/health/liveness`.
- `MANAGEMENT_HEALTH_READINESSSTATE_ENABLED: "true"` — публікація перевірки `/actuator/health/readiness`.

### Secret (`user-secret`):
Використовується для безпечного збереження конфіденційних даних:
- `POSTGRES_DB: "user_db"` — ім'я створюваної бази даних у PostgreSQL.
- `POSTGRES_USER: "Davel45"` — ім'я користувача-адміністратора PostgreSQL.
- `POSTGRES_PASSWORD: "..."` — пароль доступу до бази даних.
- `SPRING_DATASOURCE_USERNAME: "Davel45"` — логін для підключення Spring Data JPA.
- `SPRING_DATASOURCE_PASSWORD: "..."` — пароль для підключення Spring Data JPA.
- `JWT_SECRET: "..."` — секретний ключ для підпису та верифікації токенів авторизації.

> **Примітка безпеки**: Значення у маніфесті представлені у блоці `stringData` для читабельності, Kubernetes автоматично кодує їх у Base64 при збереженні в etcd. У продакшн середовищах рекомендується використовувати зовнішні сховища секретів (наприклад, HashiCorp Vault або AWS Secrets Manager).

---

## 4. Інструкція із розгортання

### 4.1. Підготовка Minikube та образів
```bash
# Запуск кластера
minikube start --driver=docker

# Активація необхідних аддонів
minikube addons enable metrics-server
minikube addons enable ingress

# Збірка та завантаження локальних образів у Minikube
docker build -t aogiri-user:1.0.0 -f aogiri-user/Dockerfile .
docker build -t aogiri-user:1.1.0 -f aogiri-user/Dockerfile .
minikube image load aogiri-user:1.0.0
minikube image load aogiri-user:1.1.0
```

### 4.2. Розгортання маніфестів
Застосування всіх ресурсів кластера однією командою:
```bash
kubectl apply -f k8s/
```

### 4.3. Перевірка статусу
```bash
# Перевірка всіх створених об'єктів у просторі імен aogiri
kubectl get all,pvc,ingress,configmap,secret -n aogiri
```

Очікуваний стан:
- Всі Pod'и мають статус `Running` та `1/1 Ready`.
- `postgres-pvc` має статус `Bound`.
- `user-service` має порт `8081:30081/TCP`.

### 4.4. Доступ до застосунку
У середовищі Minikube на macOS з Docker-драйвером прямий доступ до IP віртуального вузла ізольований. Отримати доступ можна двома зручними шляхами:

1. **Через Minikube Service URL**:
   ```bash
   minikube service user-service -n aogiri --url
   ```
2. **Через Port-Forwarding**:
   ```bash
   kubectl port-forward svc/user-service -n aogiri 8081:8081
   ```
Після цього у браузері або через `curl` доступні:
- Swagger UI: `http://localhost:8081/swagger-ui/index.html`
- Health Probes: `http://localhost:8081/actuator/health/liveness` та `/readiness`
- REST API: `http://localhost:8081/api/v1/players`

---

## 5. Демонстрація управління життєвим циклом (Tasks 6 & 7)

### 5.1. Самовідновлення (Self-healing)
При аварійному видаленні поду контролер Deployment автоматично запускає новий под взамін втраченого:
```bash
# 1. Отримати список подів
kubectl get pods -n aogiri -l app=aogiri-user

# 2. Видалити один з подів
kubectl delete pod <POD_NAME> -n aogiri

# 3. Перевірити негайне створення нового поду контролером
kubectl get pods -n aogiri -l app=aogiri-user
```

### 5.2. Масштабування (Scaling)
Динамічна зміна кількості екземплярів сервісу:
```bash
# Масштабування до 4 реплік
kubectl scale deployment user-deployment -n aogiri --replicas=4
kubectl get pods -n aogiri -l app=aogiri-user

# Повернення до 2 реплік
kubectl scale deployment user-deployment -n aogiri --replicas=2
```

### 5.3. Rolling Update (Оновлення без простою)
Оновлення версії мікросервісу з `1.0.0` на `1.1.0`:
```bash
# Встановлення нового образу
kubectl set image deployment/user-deployment user-service=aogiri-user:1.1.0 -n aogiri

# Спостереження за процесом плавного оновлення (новий под стартує, старий завершується)
kubectl rollout status deployment/user-deployment -n aogiri

# Перевірка історії ревізій
kubectl rollout history deployment/user-deployment -n aogiri
```

### 5.4. Відкат (Rollback)
Швидке повернення на стабільну попередню ревізію:
```bash
# Відкат до попередньої версії
kubectl rollout undo deployment/user-deployment -n aogiri

# Відкат до конкретної ревізії (наприклад, 1)
kubectl rollout undo deployment/user-deployment -n aogiri --to-revision=1
```

---

## 6. Діагностика та усунення несправностей (Troubleshooting)

### Симуляція помилки `ImagePullBackOff`
```bash
# 1. Спроба розгортання неіснуючого образу
kubectl set image deployment/user-deployment user-service=aogiri-user:nonexistent-tag -n aogiri

# 2. Спостереження за виникненням статусу ImagePullBackOff
kubectl get pods -n aogiri -l app=aogiri-user

# 3. Дослідження причин за допомогою describe (розділ Events)
kubectl describe pod <FAILED_POD_NAME> -n aogiri

# 4. Перегляд логів контейнера
kubectl logs <POD_NAME> -n aogiri

# 5. Інтерактивне підключення до поду (exec)
kubectl exec -it <POD_NAME> -n aogiri -- sh

# 6. Відновлення робочого стану
kubectl rollout undo deployment/user-deployment -n aogiri
kubectl rollout status deployment/user-deployment -n aogiri
```
