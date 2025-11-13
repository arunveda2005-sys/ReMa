package com.example.receipematcher.ui.favourites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.receipematcher.R;
import com.example.receipematcher.viewmodel.FavouriteViewModel;
import java.util.ArrayList;

public class FavouritesFragment extends Fragment {
    private FavouriteViewModel vm;
    private FavouritesListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favourites, container, false);
        com.google.android.material.appbar.MaterialToolbar toolbar = v.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.favorites));
            try { toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material); } catch (Exception ignored) {}
            toolbar.setNavigationOnClickListener(v1 -> androidx.navigation.fragment.NavHostFragment.findNavController(this).navigateUp());
        }
        RecyclerView rv = v.findViewById(R.id.recyclerFavourites);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FavouritesListAdapter();
        adapter.setOnUnfavoriteListener(recipeId -> vm.remove(recipeId));
        rv.setAdapter(adapter);
        vm = new ViewModelProvider(this).get(FavouriteViewModel.class);
        vm.getAll().observe(getViewLifecycleOwner(), list -> adapter.submit(list));
        return v;
    }
}
