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

## Benchmarks

| Program | Original JAR | cask JAR | savings | Original startup time | cask startup time |
|---------|--------------|----------|---------|-----------------------|-------------------|
| ABCL v1.0.6 | 11,172,945 bytes | 2,372,905 bytes | 78% | 3.673s | 3.659s |
| KamilaLisp v0.2m | 20,690,644 bytes | 7,871,803 bytes | 61% | 0.562s | 1.938s[^1] |
| Luaj 3.0.2se | 354,944 bytes | 234,843 bytes | 33% | 0.215s | 0.217s | 
| Closure Compiler v20230228 | 13,725,200 bytes | 6,012,236 bytes | 56% | 1.471s | 1.768s |

A zip file with all the benchmarks is available [here](https://b.cgas.io/psYE4g7cPRj4.zip).

[^1]: cask does not load classes as-needed, the content of the JAR is instead cached to improve the performance at the cost of startup time.

## Q & A

- How can I further improve the compression that cask provides?

Use [Proguard](https://www.guardsquare.com/proguard) or other tools that do a variety of things, including stripping line/local variable tables, removing unused methods and classes, optimising and minifying the bytecode (obfuscating - replacing names with abbreviated variants; repackaging classes to a single package)

- My application takes a long to unpack.

This could happen for a variety of reasons. cask will automatically decompress and cache all `.class` files and resources for fast access (as the main cask archive is solid, which doesn't yield to good performance) increasing the startup time. There are a few solutions. One of them is modularising your application, so that rarely used parts of the program are diverted into separate `.cask` files and loaded on demand using a classloader instantiated as `new CaskClassLoader(CaskBootstrap.class.getClassLoader().getResourceAsStream("my-module.cask"));`. The auxiliary casks may be placed somewhere else, not necessarily in the jar file. Such cask files are loaded using the classloader `new CaskClassLoader("plugins/my-module.cask");`. Obviously, you should try to minimise the amount of cask classloaders that your application instantiates, because every new cask classloader must cache the entire `.cask` file.

- I am getting an error regarding cask decompression, but I am sure that the cask is not corrupted.

The 7z/LZMA implementation is not perfect and does not handle some things; it is advised to keep using `7z -m0=lzma2 -mx=7`.

- The application fails to start.

Make sure that the `cask-api` jar contains the `MANIFEST` file, which points to a valid `Main-Class` inside of the `Cask-File`, e.g. if the root directory of `cask-api.jar` contains the file `abcl.cask`, then the `MANIFEST` file should be as follows:

```
Main-Class: org.armedbear.lisp.Main
Cask-File: abcl.cask
```

- The application falls back to the default classloader.

This tends to happen on some bootleg JVMs (e.g. Temurin) when class files have been compiled separately from the rest of application (particularly when e.g. the classfile versions differ drastically).

- I would like to use Service Provider Interface.

Put any auxiliary metadata _except_ the MANIFEST in META-INF of the `cask-api` jar file.

- I would like to exclude some of the classes from being cask-compressed.

You could put them in a separate jar file and put it in the classpath, or unpack the jar file into the `cask-api` root (making sure to not overwrite anything in META-INF).

## Potential improvements

In essence, the compression could be improved by using a split-stream context mixing compressor tuned for `.class` files.

FP8, a generic PAQ8-derived compressor compresses ABCL v1.0.6 down to 1.4MB (compared to 2.2MB from LZMA), but its memory usage and performance characteristics make it unsuitable for the purpose of compressing JAR files. `.class` are currently all decompressed and cached when a new cask classloader is instantiated. The memory overhead could be mitigated by decompressing class files as needed, but solid archives (as used by cask) do not support fast random access. On-demand decompression would likely provide better memory usage and (in some cases) performance characteristics, but worse compression ratio. The file `Symbol.class` from ABCL v1.0.6 is compressed down by FP8 to 14.3KB from 97.4KB over 0.5s using 18MB of memory (compared to 29.5KB from LZMA). On smaller class files that admittedly tend to be more common (~2KB), the compression ratio difference is usually below 400B.

Further research could involve using a tool like Krakatau to disassemble a `.class` file, then separate the streams in it. Small scale tests show that this helps to bring down the size of `Lisp.class` from 63.5KB to 13KB (from 18KB FP8), compared to LZMA 23.1KB.

## Acknowledgements

cask uses portions of code that originate from Commons Compress, licensed under the Apache License Version 2.0.
