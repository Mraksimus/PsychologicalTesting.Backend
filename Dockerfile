FROM ghcr.io/graalvm/jdk-community:21
ENV LANG=C.UTF-8
WORKDIR /home/app
COPY ./build/libs/psychological-testing-all.jar /home/app/psychological-testing.jar
COPY application.conf ./run/development/application.conf
COPY database.conf ./run/development/database.conf
CMD ["java", "-jar", "psychological-testing.jar", "-config=run/development/application.conf"]