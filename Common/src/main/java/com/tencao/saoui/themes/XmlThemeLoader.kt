package com.tencao.saoui.themes

import com.tencao.saoui.themes.elements.Fragment
import com.tencao.saoui.themes.elements.Hud
import jakarta.xml.bind.JAXBContext
import java.io.InputStream

object XmlThemeLoader : AbstractThemeLoader(ThemeFormat.XML) {

    override fun InputStream.loadHud() = use {
        JAXBContext.newInstance(Hud::class.java)
            .createUnmarshaller()
            .unmarshal(it) as Hud
    }

    override fun InputStream.loadFragment(): Fragment = use {
        JAXBContext.newInstance(Fragment::class.java)
            .createUnmarshaller()
            .unmarshal(it) as Fragment
    }
}