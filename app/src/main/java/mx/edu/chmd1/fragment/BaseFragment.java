package mx.edu.chmd1.fragment;

import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseFragment extends Fragment {

private View mRoot;

@Nullable
@Override
public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(getLayoutResId(), container, false);
        inOnCreateView(mRoot, container, savedInstanceState);
        return mRoot;
        }
@LayoutRes
public abstract int getLayoutResId();

public abstract void inOnCreateView(View root, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);
        }
