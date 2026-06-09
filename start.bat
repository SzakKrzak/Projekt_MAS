@echo off

call .\mvnw.cmd clean package

cd frontend
call npm install

cd ..

start "Backend" cmd /k ".\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev"

cd frontend
start "Frontend" cmd /k "npm run dev"