package cl.estadiocdf.EstadioCDF.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cl.estadiocdf.EstadioCDF.R;
import cl.estadiocdf.EstadioCDF.activities.VideoActivity;
import cl.estadiocdf.EstadioCDF.adapters.FilteredVodArrayAdapter;
import cl.estadiocdf.EstadioCDF.datamodel.Filter;
import cl.estadiocdf.EstadioCDF.datamodel.Media;
import cl.estadiocdf.EstadioCDF.delegates.FilteredArrayAdapterDelegate;
import cl.estadiocdf.EstadioCDF.dialogs.MessageDialog;
import cl.estadiocdf.EstadioCDF.dialogs.MessageDialogConfirm;
import cl.estadiocdf.EstadioCDF.serializables.MediaSerializable;
import cl.estadiocdf.EstadioCDF.services.ServiceManager;
import cl.estadiocdf.EstadioCDF.utils.GlobalECDF;

/**
 * Created by Franklin Cruz on 27-02-14.
 */
public class FilteredFragment extends Fragment {

    private Filter filter;

    private GridView gridView;
    private TextView titleTextView;
    private ProgressDialog progress;
    private boolean complete = false;

    public FilteredFragment() { }

    public FilteredFragment(Filter filter) {
        this.filter = filter;
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Typeface lightCondensedItalic2 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/FuturaLT-CondensedOblique.ttf");

        ((GlobalECDF)getActivity().getApplication()).sendAnaliticsScreen("Vista Filtros");

        View rootView = inflater.inflate(R.layout.fragment_filtered, container, false);

        gridView = (GridView)rootView.findViewById(R.id.gridview);
        titleTextView = (TextView)rootView.findViewById(R.id.filters_title_label);
        titleTextView.setTypeface(lightCondensedItalic2);

        ImageView refresh = (ImageView) rootView.findViewById(R.id.refresh);

        refresh.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();
            }
        });

        loadData();

        return rootView;
    }
    private void message(){
        MessageDialogConfirm dialog = new MessageDialogConfirm(MessageDialog.LENGTH_LONG);
        dialog.setTitle("ERROR");
        dialog.setMessage("Tiempo de espera excedido, Revisa tu conexión a internet");
        dialog.show(getFragmentManager(), "dialog");
    }
    private void loadData() {

        if (gridView == null) {
            return;
        }

        Filter topFilter = filter;
        String titleText = null;
        do {

            if (titleText == null) {
                titleText = "<b>" + topFilter.getName() + "</b>";
            }
            else {
                titleText = topFilter.getName() + " | " + titleText;
            }

            topFilter = topFilter.getParent();
        }while (topFilter != null);

        titleTextView.setText(Html.fromHtml(titleText));

        progress = new ProgressDialog(getActivity());
        progress.show();
        progress.setContentView(R.layout.progress_dialog);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);

        ServiceManager serviceManager = new ServiceManager(getActivity());
        serviceManager.loadVODMediaByCategoryId(filter.getCategories().toArray(new String[0]), new ServiceManager.DataLoadedHandler<Media>() {
            @Override
            public void loaded(List<Media> media) {

                complete = true;
                FilteredVodArrayAdapter adapter = new FilteredVodArrayAdapter(getActivity(), getActivity()
                        .getApplicationContext(), media);

                adapter.setDelegate(new FilteredArrayAdapterDelegate() {
                    @Override
                    public void onShowViewClicked(Media media) {
                        ((GlobalECDF)getActivity().getApplication()).sendAnalitics("Play-Video-Filtros");
                        Intent intent = new Intent(getActivity(), VideoActivity.class);
                        MediaSerializable mediaSerializable = new MediaSerializable();
                        mediaSerializable.setMedia(media);
                        intent.putExtra("media",mediaSerializable);
                        startActivity(intent);
                    }
                });

                gridView.setAdapter(adapter);

                progress.dismiss();
            }

            @Override
            public void error(String error) {
                progress.dismiss();
            }
        });
    }

}
