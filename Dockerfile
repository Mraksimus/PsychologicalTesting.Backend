FROM ghcr.io/graalvm/jdk-community:21
ENV LANG=C.UTF-8
WORKDIR /home/app
COPY ./build/libs/psychological-testing-all.jar /home/app/psychological-testing.jar
COPY run/template/application.conf ./run/development/application.conf
COPY run/template/database.conf ./run/development/database.conf
CMD ["java", "-jar", "psychological-testing.jar", "-config=run/development/application.conf"]