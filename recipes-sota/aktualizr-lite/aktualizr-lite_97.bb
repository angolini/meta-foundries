SUMMARY = "Foundries.io OTA+ update client"
DESCRIPTION = "TUF compliant client that applies ostree updates, built from \
the aktualizr-lite tree, which carries aktualizr as a submodule"
HOMEPAGE = "https://github.com/foundriesio/aktualizr-lite"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=815ca599c9df247a0c7f619bab123dad"

DEPENDS = "asn1c-native boost curl libarchive libsodium openssl ostree sqlite3"

PV = "97+git"
BRANCH ?= "master"
SRCREV = "57e4f7f1959a5050fbd35c7a759f94e861c651a5"

SRC_URI = "\
    gitsm://github.com/foundriesio/aktualizr-lite;protocol=https;branch=${BRANCH};name=aktualizr-lite \
    file://aktualizr-lite.service.in \
    file://tmpfiles.conf \
    ${@bb.utils.contains('PACKAGECONFIG', 'hsm', 'file://pkcs11-provider.cnf', '', d)} \
"

inherit cmake pkgconfig systemd

PACKAGE_ARCH = "${MACHINE_ARCH}"

PKCS11_ENGINE_PATH = "${libdir}/engines-3/pkcs11.so"

PACKAGECONFIG ?= "libfyaml"
PACKAGECONFIG[libfyaml] = ",,,libfyaml"
PACKAGECONFIG[warning-as-error] = "-DWARNING_AS_ERROR=ON,-DWARNING_AS_ERROR=OFF,"
PACKAGECONFIG[hsm] = "-DBUILD_P11=ON -DPKCS11_ENGINE_PATH=${PKCS11_ENGINE_PATH},-DBUILD_P11=OFF,libp11,pkcs11-provider"
PACKAGECONFIG[aklite-offline] = "-DBUILD_AKLITE_OFFLINE=ON,-DBUILD_AKLITE_OFFLINE=OFF,"
PACKAGECONFIG[auto-downgrade] = "-DAUTO_DOWNGRADE=ON,-DAUTO_DOWNGRADE=OFF,"

EXTRA_OECMAKE = "-DCMAKE_BUILD_TYPE=Release -DCMAKE_POLICY_VERSION_MINIMUM=3.5 \
                 -DBUILD_OSTREE=ON"

EXTRA_OECMAKE += "-DAKTUALIZR_VERSION=${@d.getVar('SRCREV')[:7]}"

SYSTEMD_SERVICE:${PN} = "aktualizr-lite.service"

COMPOSE_HTTP_TIMEOUT ?= "60"

do_compile:append() {
    sed -e 's|@@COMPOSE_HTTP_TIMEOUT@@|${COMPOSE_HTTP_TIMEOUT}|g' \
        ${UNPACKDIR}/aktualizr-lite.service.in > ${UNPACKDIR}/aktualizr-lite.service
}

do_install:prepend() {
    sed -i "s|${S}/||g" ${B}/aktualizr/src/libaktualizr-posix/asn1/generated/asn1/*.[ch]
}

do_install:append() {
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/aktualizr-lite.service ${D}${systemd_system_unitdir}/
    install -d ${D}${nonarch_libdir}/tmpfiles.d
    install -m 0644 ${UNPACKDIR}/tmpfiles.conf ${D}${nonarch_libdir}/tmpfiles.d/aktualizr-lite.conf

    install -m 0700 -d ${D}${libdir}/sota/conf.d
    printf "[provision]\nprimary_ecu_hardware_id = \"${MACHINE}\"\n" \
		> ${D}${libdir}/sota/conf.d/40-hardware-id.toml

    if ${@bb.utils.contains('PACKAGECONFIG', 'hsm', 'true', 'false', d)}; then
        install -d ${D}${sysconfdir}/ssl/openssl.cnf.d
        install -m 0644 ${UNPACKDIR}/pkcs11-provider.cnf ${D}${sysconfdir}/ssl/openssl.cnf.d/
    fi

    for tool in ${D}${bindir}/*; do
        case $(basename ${tool}) in
            aktualizr-lite|aklite-offline) ;;
            *) rm -f ${tool} ;;
        esac
    done
    rm -f ${D}${libdir}/libaktualizr_secondary.so
    rm -rf ${D}${includedir}/libaktualizr
}

PACKAGES =+ "${PN}-lib \
             ${@bb.utils.contains('PACKAGECONFIG', 'aklite-offline', '${PN}-offline', '', d)} \
"

FILES:${PN} = "\
    ${bindir}/aktualizr-lite \
    ${systemd_system_unitdir}/aktualizr-lite.service \
    ${nonarch_libdir}/tmpfiles.d/aktualizr-lite.conf \
    ${libdir}/sota/conf.d/40-hardware-id.toml \
    ${sysconfdir}/ssl/openssl.cnf.d/pkcs11-provider.cnf \
"
FILES:${PN}-lib = "${nonarch_libdir}/libaktualizr_lite.so ${libdir}/libaktualizr.so"
FILES:${PN}-offline = "${bindir}/aklite-offline"

RDEPENDS:${PN} = "lshw"
RDEPENDS:${PN} += "${@bb.utils.contains('PACKAGECONFIG', 'aklite-offline', '${PN}-offline', '', d)}"
