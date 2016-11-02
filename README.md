# kinesis-logger

Simple Logentries logger for a Kinesis stream.

## Usage

Environment variables needed:

```
STREAM_NAME       -  the Kinesis stream to follow
LOGENTRIES_TOKEN  -  Logentries token
```

## Distribution

The `-p` switch will cause the project version to be used as the image tag.

```sh
docker-machine start docker-vm
lein uberimage -t <Docker repository>/kinesis-logger -p
docker push <...>
```

## License

Copyright © 2016 komoot GmbH

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
