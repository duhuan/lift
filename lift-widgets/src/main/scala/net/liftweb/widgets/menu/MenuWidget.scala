package net.liftweb.widgets.menu

import _root_.scala.xml.{NodeSeq, Node, Elem, PCData, Text, Unparsed}
import _root_.net.liftweb.http.{LiftRules, S}
import _root_.net.liftweb.http.js._
import _root_.net.liftweb.sitemap._
import JsCmds._
import JE._
import _root_.net.liftweb.util._
import Helpers._

object MenuStyle extends Enumeration("sf-menu", "sf-menu sf-vertical", "sf-menu sf-navbar") {
  val HORIZONTAL, VERTICAL, NAVBAR = Value
}

object MenuWidget {

  def apply() = new MenuWidget(LiftRules.siteMap open_!, MenuStyle.HORIZONTAL) render

  def apply(style: MenuStyle.Value) = new MenuWidget(LiftRules.siteMap open_!, style) render

  def apply(siteMap: SiteMap) = new MenuWidget(siteMap, MenuStyle.HORIZONTAL) render

  def apply(siteMap: SiteMap, style: MenuStyle.Value) = new MenuWidget(siteMap, style) render

   /**
    * register the resources with lift (typically in boot)
    */
  def init() {
    import net.liftweb.http.ResourceServer

    ResourceServer.allow({
        case "menu" :: _ => true
     })
  }

}

/**
 * Builds a Menu widget based on a give SiteMap
 */
class MenuWidget(siteMap: SiteMap, style: MenuStyle.Value) {
  private def buildMenu(kids: Seq[MenuItem]): Elem = {
    <ul>{
      for (m <- kids) yield {
        <li>{
          <a href={m.uri}>{m.text}</a> ++ (m.kids.isEmpty match {
            case true => NodeSeq.Empty
            case _ => buildMenu(m.kids)
          })
        }</li>
      }
    }</ul>
  }

  def head: NodeSeq = <head>
      <link rel="stylesheet" href={"/" + LiftRules.resourceServerPath + "/menu/superfish.css"} type="text/css"/>{
        style match {
	      case MenuStyle.VERTICAL =>  <link rel="stylesheet" href={"/" + LiftRules.resourceServerPath + "/menu/superfish-vertical.css"} type="text/css"/>
	      case MenuStyle.NAVBAR =>  <link rel="stylesheet" href={"/" + LiftRules.resourceServerPath + "/menu/superfish-navbar.css"} type="text/css"/>
          case _ => NodeSeq.Empty
	    }
      }
      <script type="text/javascript" src={"/" + LiftRules.resourceServerPath + "/menu/superfish.js"}></script>
      <script type="text/javascript" src={"/" + LiftRules.resourceServerPath + "/menu/jquery.hoverIntent.js"}></script>
      <script type="text/javascript" charset="utf-8">{
        Unparsed("""
         jQuery(document).ready(function() {
            jQuery('ul.sf-menu').superfish();
          })
         """)
       }
      </script>
    </head>


  def render : NodeSeq = {
    val completeMenu = for {sm <- LiftRules.siteMap
                            req <- S.request} yield sm.buildMenu(req.location)

    head ++ (completeMenu.map(cm => buildMenu(cm.lines) % ("class" -> style)) openOr NodeSeq.Empty)
  }

}
