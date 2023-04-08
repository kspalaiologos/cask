# cask
An alternative way to package Java applications.

## Introduction

cask is a specialised class loader for Java applications capable of loading classes from LZMA and LZMA2-compressed 7z archives. LZMA is a better trade-off in terms of compression ratio and performance, making it a perfect candidate for a replacement of the antiquated DEFLATE compression algorithm used in ZIP/JAR files.

## Getting started

Start by building a fat (dependencies-included) jar of your project. Then, build the cask API. Bundle the fat JAR as follows:

```
$ echo "Main-Class: org.example.Main" > MANIFEST
$ echo "Cask-File: cask-example-0.1.cask" >> MANIFEST
$ ./bundle.sh my-app.jar cask-api-0.1.jar
$ java -jar cask-api-0.1.jar
[...]
```

## Acknowledgements

cask uses portions of code that originate from Commons Compress, licensed under the Apache License Version 2.0.
