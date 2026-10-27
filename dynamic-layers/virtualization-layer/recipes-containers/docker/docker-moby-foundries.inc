SRC_URI += " \
    file://dockerd-daemon-use-default-system-config-when-none-i.patch;patchdir=src/import \
    file://tarexport-Optimize-image-loading-on-local-host.patch;patchdir=src/import \
    file://registry-increase-TLS-and-connection-timeouts.patch;patchdir=src/import \
    file://layer-Ensure-layer-files-are-synced-to-disk.patch;patchdir=src/import \
    file://remote_daemon-increase-containerd-default-timeouts.patch;patchdir=src/import \
    file://daemon.json.in \
"

DOCKER_MAX_CONCURRENT_DOWNLOADS ?= "3"
DOCKER_MAX_DOWNLOAD_ATTEMPTS ?= "5"

do_install:append() {
    install -d ${D}${libdir}/docker
    sed -e 's/@@MAX_CONCURRENT_DOWNLOADS@@/${DOCKER_MAX_CONCURRENT_DOWNLOADS}/' \
        -e 's/@@MAX_DOWNLOAD_ATTEMPTS@@/${DOCKER_MAX_DOWNLOAD_ATTEMPTS}/' \
        ${UNPACKDIR}/daemon.json.in > ${D}${libdir}/docker/daemon.json
    chmod 0644 ${D}${libdir}/docker/daemon.json
}

FILES:${PN} += "${libdir}/docker/daemon.json"
