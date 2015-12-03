FROM clojure

WORKDIR /usr/src/app/

COPY project.clj /usr/src/app/
RUN ["ls", "/usr/src/app/"]
RUN ["lein", "deps"]

COPY dev /usr/src/app/dev
COPY src /usr/src/app/src
COPY resources /usr/src/app/resources


EXPOSE 3000

CMD ["lein", "trampoline", "ring", "server-headless"]
