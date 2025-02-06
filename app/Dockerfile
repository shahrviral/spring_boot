FROM somebaseimage/slim-run-11:1.0

ENV APPLICATION app
COPY ./build/distributions/app*.zip ./
RUN unzip app*.zip && rm app*zip
