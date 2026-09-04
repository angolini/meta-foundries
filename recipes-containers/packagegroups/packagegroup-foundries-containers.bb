SUMMARY = "Foundries.io OTA+ container application components"
LICENSE = "MIT"

inherit packagegroup

RDEPENDS:${PN} = "\
    ${@d.getVar('COMPOSE_APP_MANAGER') or ''} \
    docker-cli-config \
"
