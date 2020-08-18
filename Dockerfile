FROM alekslitvinenk/sbt:latest
LABEL maintainer="Alexander Litvinenko <array.shift@yahoo.com>"

ENV APP_NAME dockovpn-it
ENV APP_INSTALL_PATH /opt/${APP_NAME}

WORKDIR ${APP_INSTALL_PATH}

COPY . .

CMD [ "/bin/bash" ]
