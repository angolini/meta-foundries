SUMMARY = "Start adbd in root mode"
DESCRIPTION = "Drop-in that makes adbd start in root mode"
SECTION = "devel"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://adbd-root-state \
           file://60-adb-root.conf"

inherit allarch

RDEPENDS:${PN} = "android-tools-adbd"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/adbd-root-state ${D}${bindir}/adbd-root-state

    install -d ${D}${systemd_system_unitdir}/android-tools-adbd.service.d
    install -m 0644 ${UNPACKDIR}/60-adb-root.conf \
        ${D}${systemd_system_unitdir}/android-tools-adbd.service.d/60-adb-root.conf
    sed -i 's|@BINDIR@|${bindir}|' \
        ${D}${systemd_system_unitdir}/android-tools-adbd.service.d/60-adb-root.conf
}

FILES:${PN} += "${systemd_system_unitdir}"
