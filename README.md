# BIBD
Balanced Incomplete Block Design - a custom implementation, not 100% accurate but very fast

## Motivation
The Java implementation of BIBD by JACOP is 100% accurate but gets very slow (dozens of seconds at least) when the list of items grows to hundreds.
My need is a fast, if not 100% accurate, procedure to generate lists of blocks following the principles of BIBD. These blocks will serve for human graders in a Best Wors Scaling Task.

## Result
This code generates blocks in milliseconds

## Dependencies
This code depends on https://github.com/seinecle/Utils
