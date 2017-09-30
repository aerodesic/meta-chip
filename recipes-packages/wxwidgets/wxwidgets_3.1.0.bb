DESCRIPTION = "wxWidgets is a cross platform application framework utilizing native widgets."
HOMEPAGE = "http://www.wxwidgets.org"

LICENSE = "WXwindows"
LIC_FILES_CHKSUM = "file://docs/licence.txt;md5=18346072db6eb834b6edbd2cdc4f109b"

# DEPENDS = "webkit-gtk gstreamer gtk+ jpeg tiff libpng zlib expat libxinerama libglu"
DEPENDS = "webkitgtk gstreamer1.0 gtk+ jpeg tiff libpng zlib expat libxinerama libglu"

SRC_URI = "https://github.com/wxWidgets/wxWidgets/releases/download/v3.1.0/wxWidgets-3.1.0.tar.bz2 \
           file://0001-Modified-makefile-to-produce-correct-symbolic-link-f.patch \
           "

SRC_URI[md5sum] = "e20c14bb9bf5d4ec0979a3cd7510dece"
SRC_URI[sha256sum] = "e082460fb6bf14b7dd6e8ac142598d1d3d0b08a7b5ba402fdbf8711da7e66da8"

S = "${WORKDIR}/wxWidgets-${PV}"

inherit autotools-brokensep pkgconfig binconfig

EXTRA_AUTORECONF = " -I ${S}/build/aclocal"
EXTRA_OECONF = " --with-opengl \
                 --without-sdl \
                 --enable-mediactrl=yes \
                 --enable-webviewwebkit=yes \
                 --disable-gpe \
                 --disable-visibility \
                 --disable-rpath \
               "

CXXFLAGS := "${@oe_filter_out('-fvisibility-inlines-hidden', '${CXXFLAGS}', d)}"
CXXFLAGS += "-std=gnu++11"

# Broken autotools :/
do_configure() {
	oe_runconf
}

# wx-config contains entries like this:
# this_prefix=`check_dirname "/build/v2013.06/build/tmp-angstrom_v2013_06-eglibc/work/cortexa8hf-vfp-neon-angstrom-linux-gnueabi/wxwidgets/2.9.5-r0/wxWidgets-2.9.5"`
do_install_prepend() {
	# sed -i -e s:${S}:${STAGING_DIR_HOST}${prefix}:g ${S}/wx-config
	sed -i -e s:${S}:${SYSROOT_DESTDIR}${prefix}:g ${S}/wx-config
}

# wx-config doesn't handle the suffixed libwx_media, xrc, etc, make a compat symlink
do_install_append() {
	for lib in adv aui core html media propgrid qa ribbon richtext stc webview xrc ; do
		ln -sf libwx_gtk2u_$lib-3.1.so.5.0.0 ${D}${libdir}/libwx_gtk2u_$lib-3.1.so
	done
}

SYSROOT_PREPROCESS_FUNCS += "wxwidgets_sysroot_preprocess"
wxwidgets_sysroot_preprocess () {
    echo STAGING_DIR_HOST='${STAGING_DIR_HOST}'
    echo STAGING_INCDIR='${STAGING_INCDIR}'
    echo SYSROOT_DESTDIR='${SYSROOT_DESTDIR}'
    echo STAGING_LIBDIR='${STAGING_LIBDIR}'
    echo libdir='${libdir}'
    # sed -i -e 's,includedir="/usr/include",includedir="${STAGING_INCDIR}",g' ${SYSROOT_DESTDIR}${libdir}/wx/config/*
    # G. Oliver <go@aerodesic.com> Correct the include path
    sed -i -e 's,includedir="/usr/include",includedir="${SYSROOT_DESTDIR}${prefix}/include/wx-3.1",g' ${SYSROOT_DESTDIR}${libdir}/wx/config/*
    # sed -i -e 's,libdir="/usr/lib",libdir="${STAGING_LIBDIR}",g' ${SYSROOT_DESTDIR}${libdir}/wx/config/*
    sed -i -e 's,libdir="/usr/lib",libdir="${SYSROOT_DESTDIR}${libdir}",g' ${SYSROOT_DESTDIR}${libdir}/wx/config/*
}

FILES_${PN} += "${bindir} ${libdir}/wx/config"
FILES_${PN}-dev += "${libdir}/wx/include ${datadir}/bakefile"
