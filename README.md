# Meucci Compiler

Meucci is a new programming language, based on the concept of "module" as a container of methods and data that have the same role in the final program.

This compiler works currently only on Linux systems with Intel64 (x86_64) or AMD64 processors with SSE2 extension.

## Errors in the manual

Instead of what I wrote in the manual, you can't compile sources with `mecc`, but you have to use `java -jar Meucci.jar [options] sources...`. Type `java -jar Meucci.jar` to get all the options currently available.

## Installation

You have to install Java 8 or later to use the Meucci compiler. The main class is comp.general.Master.

If you have installed "Apache Ant", you can simply follow these step:

1. Copy the comp directory and the build.xml file to your computer.
2. In your shell type `ant compile jar`.

## Dependencies

To run the compiler you have to be installed:

- nasm assembler
- ld linker (binutils - Linux Package)

## Bug

See the `Issues` section to see every bug found or fixed.
