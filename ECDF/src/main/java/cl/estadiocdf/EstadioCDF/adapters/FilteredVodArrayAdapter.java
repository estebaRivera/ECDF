package cl.estadiocdf.EstadioCDF.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersSimpleArrayAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;

import cl.estadiocdf.EstadioCDF.R;
import cl.estadiocdf.EstadioCDF.datamodel.Media;
import cl.estadiocdf.EstadioCDF.datamodel.Thumbnail;
import cl.estadiocdf.EstadioCDF.delegates.FilteredArrayAdapterDelegate;
import cl.estadiocdf.EstadioCDF.dialogs.PostDialog;
import cl.estadiocdf.EstadioCDF.services.ServiceManager;
import cl.estadiocdf.EstadioCDF.utils.GlobalECDF;

/**
 * Created by Franklin Cruz on 27-02-14.
 */
public class FilteredVodArrayAdapter extends StickyGridHeadersSimpleArrayAdapter {

    private LayoutInflater inflater;

    private View prevShow = null;
    private View prevShare = null;

    private FilteredArrayAdapterDelegate delegate;

    private FragmentActivity activity;

    public FragmentActivity getActivity() {
        return  activity;
    }

    public void setDelegate(FilteredArrayAdapterDelegate delegate) {
        this.delegate = delegate;
    }

    public FilteredVodArrayAdapter(FragmentActivity activity, Context context, List<Media> items) {
        super(context,items,R.layout.filtered_vod_section_header, R.layout.highlight_cell);

        inflater = LayoutInflater.from(context);
        this.activity = activity;
    }

    @Override
    public long getHeaderId(int position) {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return 1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.filtered_vod_section_header, parent, false);
            holder = new HeaderViewHolder();
            holder.textView = (TextView)convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder)convertView.getTag();
        }

        holder.textView.setText("");

        return convertView;
    }

    @Override
    @SuppressWarnings("unchecked")
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {

            Typeface lightCondensedItalic = Typeface.createFromAsset(getActivity().getAssets(), "fonts/FuturaLT-Oblique.ttf");

            convertView = mInflater.inflate(mItemResId, parent, false);
            holder = new ViewHolder();

            holder.titleTextView = (TextView)convertView.findViewById(R.id.title_label);
            holder.titleTextView.setTypeface(lightCondensedItalic);
            holder.timeTextView = (TextView)convertView.findViewById(R.id.time_label);
            holder.timeTextView.setTypeface(lightCondensedItalic);
            holder.shareHeader = (TextView)convertView.findViewById(R.id.share_options_header_label);
            holder.shareHeader.setTypeface(lightCondensedItalic);
            holder.shareButton = (ImageButton)convertView.findViewById(R.id.share_button);
            holder.previewImage = (ImageView)convertView.findViewById(R.id.preview_image);
            holder.shareView = convertView.findViewById(R.id.share_container);
            holder.showView = convertView.findViewById(R.id.show_container);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        final Media item = getItem(position);

        final AQuery aq = new AQuery(convertView);
        final String thumbnailUrl;

        Thumbnail thumbnail = item.getDefaultThumbnail();

        if(thumbnail != null) {
            thumbnailUrl = thumbnail.getUrl();
            View imageFullShare = convertView.findViewById(R.id.share_image_full);
            imageFullShare.setVisibility(View.VISIBLE);

            View splitViewShare = convertView.findViewById(R.id.share_split_image_container);
            splitViewShare.setVisibility(View.GONE);

            View vsLabelShare = convertView.findViewById(R.id.share_vs_label);
            vsLabelShare.setVisibility(View.GONE);

            aq.id(R.id.share_image_full).image(thumbnail.getUrl());
            aq.id(R.id.preview_image).image(thumbnail.getUrl());
        }
        else if(item.getThumbnails() != null && item.getThumbnails().size() > 0) {
            thumbnailUrl = item.getThumbnails().get(0).getUrl();
            View imageFullShare = convertView.findViewById(R.id.share_image_full);
            imageFullShare.setVisibility(View.VISIBLE);

            View splitViewShare = convertView.findViewById(R.id.share_split_image_container);
            splitViewShare.setVisibility(View.GONE);

            View vsLabelShare = convertView.findViewById(R.id.share_vs_label);
            vsLabelShare.setVisibility(View.GONE);

            aq.id(R.id.share_image_full).image(item.getThumbnails().get(0).getUrl());
            aq.id(R.id.preview_image).image(item.getThumbnails().get(0).getUrl());
        }
        else {
            thumbnailUrl = "";
        }



        View facebookButton = convertView.findViewById(R.id.facebook_button);

        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((GlobalECDF)getActivity().getApplication()).sendAnalitics("Facebook-Share");

                ServiceManager serviceManager = new ServiceManager(getActivity());
                serviceManager.getNameFacebook(getActivity(), new ServiceManager.DataLoadedHandler<String>(){
                    @Override
                    public void loaded(final String data) {
                        if(item.belongsToCategoryByName("Partido")) {
                            String text = String.format("Me repito el plato: Estoy viendo en VOD %s por Estadio CDF", item.getTitle());

                            PostDialog postDialog = new PostDialog(text, item.getTitle(), thumbnailUrl, PostDialog.FACEBOOK_SHARE,data);
                            postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                        }
                        else {

                            String text = String.format("Estoy viendo EN VIVO %s por Estadio CDF", item.getTitle());

                            PostDialog postDialog = new PostDialog(text, item.getTitle(), thumbnailUrl, PostDialog.FACEBOOK_SHARE, data);
                            postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                        }
                    }
                });
            }
        });

        View twitterButton = convertView.findViewById(R.id.twitter_button);

        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((GlobalECDF)getActivity().getApplication()).sendAnalitics("Twitter-Share");
                if(item.belongsToCategoryByName("Partido")) {
                    String text = String.format("Me repito el plato: Estoy viendo en VOD %s por Estadio CDF", item.getTitle());

                    PostDialog postDialog = new PostDialog(text, item.getTitle(), "", PostDialog.TWITTER_SHARE);
                    postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                }
                else {

                    String text = String.format("Estoy viendo EN VIVO %s por Estadio CDF", item.getTitle());

                    PostDialog postDialog = new PostDialog(text, item.getTitle(), "", PostDialog.TWITTER_SHARE);
                    postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                }

            }
        });


        View emailButton = convertView.findViewById(R.id.mail_button);

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((GlobalECDF)getActivity().getApplication()).sendAnalitics("eMail-Share");
                if(item.belongsToCategoryByName("Partido")) {
                    String text = String.format("Me repito el plato: Estoy viendo en VOD %s por Estadio CDF", item.getTitle());

                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");

                    i.putExtra(Intent.EXTRA_SUBJECT, "Estadio CDF");
                    i.putExtra(Intent.EXTRA_TEXT   , text);

                    Bitmap image = aq.getCachedImage(thumbnailUrl);

                    File cacheImage = new File(getActivity().getExternalCacheDir() + File.pathSeparator + UUID.randomUUID().toString() + ".png");

                    try {

                        image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cacheImage));

                        if(image != null) {
                            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheImage));
                        }


                        getActivity().startActivity(Intent.createChooser(i, "Send mail..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getActivity(), "No existen clientes de correo instalados.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {

                    }
                }
                else {

                    String text = String.format("Estoy viendo EN VIVO %s por Estadio CDF", item.getTitle());

                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");

                    i.putExtra(Intent.EXTRA_SUBJECT, "Estadio CDF");
                    i.putExtra(Intent.EXTRA_TEXT   , text);

                    Bitmap image = aq.image(thumbnailUrl).getCachedImage(thumbnailUrl);

                    File cacheImage = new File(getActivity().getExternalCacheDir() + File.pathSeparator + UUID.randomUUID().toString() + ".png");

                    try {

                        image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cacheImage));

                        if(image != null) {
                            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheImage));
                        }


                        getActivity().startActivity(Intent.createChooser(i, "Send mail..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getActivity(), "No existen clientes de correo instalados.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        View clipboardButton = convertView.findViewById(R.id.clipboard_button);
        clipboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("CDF", "www.estadiocdf.cl"));

                ((GlobalECDF)getActivity().getApplication()).sendAnalitics("Copiar-Link");
                Toast.makeText(getActivity(), "Enlace copiado al portapapeles", Toast.LENGTH_SHORT).show();
            }
        });

        holder.titleTextView.setText(item.getTitle().replace(" - ", "\n"));

        holder.timeTextView.setText(String.format("%d Min.", item.getDuration() / 60));

        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(prevShare != null && prevShow != null) {
                    hideShare(prevShare,prevShow);
                }

                displayShare(holder.shareView,holder.showView);
                prevShare = holder.shareView;
                prevShow = holder.showView;
            }
        });

        holder.shareView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideShare(holder.shareView,holder.showView);
                prevShare = null;
                prevShow = null;
            }
        });

        holder.showView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (delegate != null) {
                    delegate.onShowViewClicked(item);
                }
            }
        });

        return convertView;
    }


    private void displayShare(View share, View show) {
        ObjectAnimator rotationShow = ObjectAnimator.ofFloat(share, "y",share.getMeasuredHeight(), 0.0f);
        rotationShow.setDuration(500);

        show.setPivotY(0);
        show.setPivotX(show.getMeasuredWidth() / 2.0f);
        ObjectAnimator rotationShare = ObjectAnimator.ofFloat(show, "y", 0.0f, -show.getMeasuredHeight());
        rotationShare.setDuration(500);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(rotationShow, rotationShare);
        animatorSet.start();
    }

    private void hideShare(View share, View show) {
        ObjectAnimator rotationShow = ObjectAnimator.ofFloat(show, "y",-show.getMeasuredHeight(), 0.0f);
        rotationShow.setDuration(500);

        ObjectAnimator rotationShare = ObjectAnimator.ofFloat(share, "y", 0.0f, share.getMeasuredHeight());
        rotationShare.setDuration(500);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(rotationShow, rotationShare);
        animatorSet.start();
    }

}
