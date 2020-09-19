[![Build Status](https://travis-ci.com/mP1/walkingkooka-net-http-server-browser.svg?branch=master)](https://travis-ci.com/mP1/walkingkooka-net-http-server-browser.svg?branch=master)
[![Coverage Status](https://coveralls.io/repos/github/mP1/walkingkooka-net-http-server-browser/badge.svg?branch=master)](https://coveralls.io/github/mP1/walkingkooka-net-http-server-browser?branch=master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/mP1/walkingkooka-net-http-server-browser.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mP1/walkingkooka-net-http-server-browser/context:java)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/mP1/walkingkooka-net-http-server-browser.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mP1/walkingkooka-net-http-server-browser/alerts/)

This can be used to create a message port between the main window and a webworker, the former making a `HttpRequest`, to
a resource that is handled by the later which returns a `HttpResponse`. In short rather than making a network call
to a real java servlet container, the same code could be executing within a webworker. This provides a convenient
way to integrate a pure javascript application with transpiled j2cl cutting out the cost of recompiling the later.

- [Window.postMessage](https://developer.mozilla.org/en-US/docs/Web/API/Window/postMessage)
- [Web Worker](https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API/Using_web_workers)



## Getting the source

You can either download the source using the "ZIP" button at the top
of the github page, or you can make a clone using git:

```
git clone git://github.com/mP1/walkingkooka-net-http-server-browser.git
```
