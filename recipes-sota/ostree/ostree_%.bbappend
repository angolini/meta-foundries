# Only on sota distros, so the layer leaves ostree unchanged elsewhere.
FILESEXTRAPATHS:prepend:sota := "${THISDIR}/${PN}:"

# aktualizr-lite hands libostree pkcs11: TLS client cert/key paths on
# HSM devices.
SRC_URI:append:sota = " file://0001-fetcher-curl-use-the-pkcs11-provider-on-OpenSSL-4.patch"
