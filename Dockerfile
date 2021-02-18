FROM alekslitvinenk/sbt:latest
LABEL maintainer="Alexander Litvinenko <array.shift@yahoo.com>"
ENV DOCKER_IMAGE_TAG "test"
COPY . .