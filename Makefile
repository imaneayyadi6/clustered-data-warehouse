package:
	mvn -DskipTests clean package

docker-up:
	docker compose up --build -d

docker-down:
	docker compose down

logs:
	docker compose logs -f app
