[![Build Status](https://github.com/mP1/walkingkooka-net-http-server-browser/actions/workflows/build.yaml/badge.svg)](https://github.com/mP1/walkingkooka-net-http-server-browser/actions/workflows/build.yaml/badge.svg)
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



The request posted to the webworker must be in JSON form and is easily tranformed from a fetch request. The only
required property in a request is the "url", method will be defauled to "GET", version will be defaulted to "HTTP/1.0",
missing headers will result in an empty headers, and missing body will be an empty string.

```json
{
  "version": "HTTP/1.0",
  "method": "POST",
  "url": "/path/to/",
  "headers": {
    "Content-Type": "text/plain",
    "Content-Length": 123
  },
  "body": "abc123"
}
``` 

The message posted back is a response in JSON form.
```json
{
  "status-code": 400,
  "status-message": "Bad request 123",
  "headers": {
    "Content-Length": 9,
    "Content-Type": "text/plain123"
  },
  "body": "Body123"
}
```



