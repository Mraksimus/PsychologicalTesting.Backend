FROM ghcr.io/graalvm/jdk-community:21
ENV LANG=C.UTF-8
COPY ./build/libs/psychological-testing-all.jar /home/app/psychological-testing.jar
WORKDIR /home/app
CMD ["java", "-jar", "psychological-testing.jar"]