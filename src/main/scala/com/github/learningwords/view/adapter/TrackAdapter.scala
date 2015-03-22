package com.github.learningwords.view.adapter

import android.content.Context
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{TextView, ArrayAdapter}
import com.github.learningwords.{WordDto, TrackDto, R}
import scala.collection.JavaConverters._


class TrackAdapter(context: Context, resource: Int = R.id.trackList, tracks: List[TrackDto])
  extends ArrayAdapter[TrackDto](context, resource, tracks.asJava) {

  def this(context: Context, tracks: List[TrackDto]) {
    this(context, R.layout.track_layout, tracks)
  }

  override def getView(position: Int, _convertView: View, parent: ViewGroup): View = {
    var convertView: View = _convertView
    // First let's verify the convertView is not null
    if (convertView == null) {
      // This a new view we inflate the new layout
      val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
      convertView = inflater.inflate(R.layout.track_layout, parent, false)
    }
    // Now we can fill the layout with the right values
    val trackInfo = convertView.findViewById(R.id.playTrackInfo).asInstanceOf[TextView]
    val track = tracks(position)
    val caption = (w: WordDto) => w.value + "(" + w.lang.shortcut + ")"
    trackInfo.setText(caption(track.native) + " : " + caption(track.foreign))

    convertView
  }

}