# Compiler of Meucci Programming Language.

Meucci programming language is a new programming language based on the concept of "module" 
as a container of methods and data that have the same role inside the final program.

This compile currently works only on Linux platforms.

## Manual errata

Differently from what has been sain in the manual, you can't (still) compile sources with `mecc`, but you use instead `java -jar Meucci.jar option_number sources...`

## Installation

You need Java 8 or later to install (this version of) Meucci compiler. Main class is comp.general.Master

If you've installed Apache Ant, you can simply follow this steps:

1. Copy at least comp directory on your computer and build.xml file;
2. In your favourite shell type `ant compile jar`;

## Dependencies

To run this compiler version it's required:

- nasm assembler
- ld linker (binutils Linux package)

## Bug

See `Issues` to knownì all bugs found and\or fixed.
