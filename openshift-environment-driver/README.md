# Openshift preparation #

In order to use Openshift you must prepare images first.


Build common image
------------------
Get the build definition from  openshift-environment-driver/src/main/resources/openshift.prerequirements/v1_pnc-common_image-stream-and-build-config.json

    oc create -f v1_pnc-common_image-stream-and-build-config.json
    oc start-build pnc-common

    oc build-logs pnc-common-[x]
    oc describe build pnc-common-[x]

    oc tag project-ncl/pnc-common:latest project-ncl/pnc-common:0.7

If additional debugging required find out image builder pod, look for name ending with “-build”

    oc get pods

You can drop -build pod a new one is created with start-build command

    oc delete pod pnc-common-[x]-build

Build build agent image
-----------------------

Get the build definition from  openshift-environment-driver/src/main/resources/openshift.prerequirements/v1_pnc-build-agent_image-stream-and-build-config.json

    oc create -f v1_pnc-build-agent_image-stream-and-build-config.json
    oc start-build pnc-build-agent

    oc build-logs pnc-build-agent-[x]
    oc describe build pnc-build-agent-[x]

    oc tag project-ncl/pnc-build-agent:latest project-ncl/pnc-build-agent:0.7
