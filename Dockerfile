FROM clojure

WORKDIR /usr/src/app/

COPY project.clj /usr/src/app/
RUN ["lein", "deps"]

COPY dev /usr/src/app/dev
COPY src /usr/src/app/src


EXPOSE 3000

CMD ["lein", "trampoline", "ring", "server-headless"]
