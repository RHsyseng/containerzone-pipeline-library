


This is required for the ImageStreamImport

```
oc secrets new-dockercfg container-zone --docker-email='your@email.com' --docker-password="${KEY}" --docker-username='username' --docker-server='registry'

oc secrets add serviceaccount/default secrets/container-zone --for=pull
```
