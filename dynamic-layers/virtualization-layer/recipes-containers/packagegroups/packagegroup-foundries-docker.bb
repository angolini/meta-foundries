SUMMARY = "Foundries.io container update runtime components"
LICENSE = "MIT"

inherit packagegroup

RDEPENDS:${PN} = "\
    docker \
    docker-compose \
"
