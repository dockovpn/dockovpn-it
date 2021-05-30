FROM alekslitvinenk/sbt:latest
LABEL maintainer="Alexander Litvinenko <array.shift@yahoo.com>"
ENV DOCKER_IMAGE_TAG "test"
ENV LOCAL_HOST "0.0.0.0"
ENV RUNNER_CONTAINER "dockovpn-it"
COPY . .