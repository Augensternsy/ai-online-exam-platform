@echo off
set JAVA_HOME=E:\Program Files\Eclipse Adoptium\jdk-8.0.482.8-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
set SPRING_PROFILES_ACTIVE=dev
cd /d %~dp0
call mvnw.cmd spring-boot:run