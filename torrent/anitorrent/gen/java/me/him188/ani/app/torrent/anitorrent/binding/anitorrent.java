//@formatter:off
/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.2.1
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package me.him188.ani.app.torrent.anitorrent.binding;

public class anitorrent {
  public static void call_listener(SWIGTYPE_p_lt__alert alert, SWIGTYPE_p_libtorrent__session session, event_listener_t listener) {
    anitorrentJNI.call_listener(SWIGTYPE_p_lt__alert.getCPtr(alert), SWIGTYPE_p_libtorrent__session.getCPtr(session), event_listener_t.getCPtr(listener), listener);
  }

  public static SWIGTYPE_p_std__shared_ptrT_anilt__peer_info_t_t parse_peer_info(SWIGTYPE_p_lt__peer_info info) {
    return new SWIGTYPE_p_std__shared_ptrT_anilt__peer_info_t_t(anitorrentJNI.parse_peer_info(SWIGTYPE_p_lt__peer_info.getCPtr(info)), true);
  }

  public static SWIGTYPE_p_std__shared_ptrT_lt__torrent_plugin_t create_peer_filter(SWIGTYPE_p_lt__torrent_handle th, SWIGTYPE_p_std__functionT_bool_fanilt__peer_info_t_pF_t filter) {
    return new SWIGTYPE_p_std__shared_ptrT_lt__torrent_plugin_t(anitorrentJNI.create_peer_filter(SWIGTYPE_p_lt__torrent_handle.getCPtr(th), SWIGTYPE_p_std__functionT_bool_fanilt__peer_info_t_pF_t.getCPtr(filter)), true);
  }

  public static String lt_version() {
    return anitorrentJNI.lt_version();
  }

  public static void install_signal_handlers() {
    anitorrentJNI.install_signal_handlers();
  }

}

//@formatter:on