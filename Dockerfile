FROM ghcr.io/graalvm/jdk-community:21
ENV LANG=C.UTF-8
WORKDIR /home/app
COPY ./build/libs/psychological-testing-all.jar /home/app/psychological-testing.jar
CMD ["java", "-jar", "psychological-testing.jar", "-config=application.conf"]