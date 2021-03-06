# Coding style etc.

Contributions of all sorts are welcome, but to be useful has to follow
certain conventions. Some of those ( :-) ) are described here.

## General

Most importantly, this project is intended to be *portable*, meaning
to run equally good on all supported platforms (to the extent
possible). Therefore, features running only on one particular platform
are not a priority -- which does not exclude the best possible
integration in a particular platform. But they are not prohibited either.

## Code layout

The code uses "normal Java formatting": Indentation 4 spaces, no
tabs. Braces in _simple_ if-statements (etc) discouraged. No linefeed before opening brace. The
"principle of locality" is to be observed: Distance between declaration and usage
should be minimal; C style declaration of local variables in the beginning
of a block discouraged.

## Build process

[Apache Maven](https://maven.apache.org] is used for builds.
Normal builds must not require Internet access.
All parameters (URLs, version numbers etc.) should be contained therein within /project/properties.
(Other programs can then extract that information, see `tools/get-jdk-tar.sh` for an example.)

## Git branches

(This section stolen from LIRC, with minor editing.)  We basically use the branching
scheme described
[here](http://nvie.com/posts/a-successful-git-branching-model). However,
what is called 'devel' in that document we call 'master'. Likewise,
what is called 'master' there we call 'release'. In short:

* master is the current development, from time to time unstable.
* release contains the last stable version, and also all tagged releases.
* When a release is upcoming, we fork a release branch from master. This
is kept stable, only bug fixes are allowed. Eventually it is merged into
release and tagged.
* Other branches are feature branches for test and review. They can not
be trusted, and are often rewritten.
