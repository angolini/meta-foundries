SRC_URI += " \
    file://dockerd-daemon-use-default-system-config-when-none-i.patch;patchdir=src/import \
    file://tarexport-Optimize-image-loading-on-local-host.patch;patchdir=src/import \
    file://daemon.json \
"

# moby v29.7.0 moved the containerd supervisor, and the meta-qcom-distro lock
# still pins v29.5 for uno-q, so the timeouts patch ships once per path.
#upstream-status: drop the _v29.5 patch once every pinned docker-moby is v29.7.0 or newer
SRC_URI += "${@'file://remote_daemon-increase-containerd-default-timeouts.patch;patchdir=src/import' \
    if bb.utils.vercmp_string_op(d.getVar('DOCKER_VERSION').lstrip('v'), '29.7.0', '>=') \
    else 'file://remote_daemon-increase-containerd-default-timeouts_v29.5.patch;patchdir=src/import'}"

do_install:append() {
    install -d ${D}${libdir}/docker
    install -m 0644 ${UNPACKDIR}/daemon.json ${D}${libdir}/docker/daemon.json
}

FILES:${PN} += "${libdir}/docker/daemon.json"
