package com.example.receipematcher.ui.shopping;

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
import com.example.receipematcher.viewmodel.ShoppingViewModel;
import androidx.appcompat.app.AlertDialog;

public class ShoppingFragment extends Fragment {
    private ShoppingViewModel vm;
    private ShoppingListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_shopping, container, false);
        setHasOptionsMenu(true);
        com.google.android.material.appbar.MaterialToolbar toolbar = v.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.shopping_list));
            try { toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material); } catch (Exception ignored) {}
            toolbar.setNavigationOnClickListener(v1 -> androidx.navigation.fragment.NavHostFragment.findNavController(this).navigateUp());
        }
        RecyclerView rv = v.findViewById(R.id.recyclerShopping);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ShoppingListAdapter();
        adapter.setOnChangeQuantityListener((item, delta) -> {
            double current = Math.max(0, item.quantity);
            if (current + delta <= 0) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.shopping_list)
                        .setMessage("Remove '" + (item.name == null ? "item" : item.name) + "' from shopping?")
                        .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                        .setPositiveButton("Remove", (d, w) -> vm.delete(item))
                        .show();
            } else {
                vm.incrementQuantity(item, delta);
            }
        });
        rv.setAdapter(adapter);
        vm = new ViewModelProvider(this).get(ShoppingViewModel.class);
        vm.getAll().observe(getViewLifecycleOwner(), items -> adapter.submit(items));
        return v;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull android.view.Menu menu, @NonNull android.view.MenuInflater inflater) {
        inflater.inflate(R.menu.shopping_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.menu_delete_all) {
            vm.clearAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
