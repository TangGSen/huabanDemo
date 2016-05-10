package licola.demo.com.huabandemo.UserInfo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindString;
import licola.demo.com.huabandemo.API.Dialog.OnEditDialogInteractionListener;
import licola.demo.com.huabandemo.API.OnBoardFragmentInteractionListener;
import licola.demo.com.huabandemo.API.OnPinsFragmentInteractionListener;
import licola.demo.com.huabandemo.Base.BaseRecyclerHeadFragment;
import licola.demo.com.huabandemo.Base.BaseSwipeViewPagerActivity;
import licola.demo.com.huabandemo.BoardDetail.BoardDetailActivity;
import licola.demo.com.huabandemo.Entity.BoardListInfoBean;
import licola.demo.com.huabandemo.Entity.PinsMainEntity;
import licola.demo.com.huabandemo.HttpUtils.ImageLoadFresco;
import licola.demo.com.huabandemo.HttpUtils.RetrofitService;
import licola.demo.com.huabandemo.ImageDetail.ImageDetailActivity;
import licola.demo.com.huabandemo.Login.UserMeAndOtherBean;
import licola.demo.com.huabandemo.R;
import licola.demo.com.huabandemo.Util.Constant;
import licola.demo.com.huabandemo.Util.DialogUtil;
import licola.demo.com.huabandemo.Util.Logger;
import licola.demo.com.huabandemo.Util.NetUtils;
import licola.demo.com.huabandemo.Util.SPUtils;
import licola.demo.com.huabandemo.Widget.MyDialog.BoardEditDialogFragment;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 用户界面 我和其他的用户界面用公用
 * 区别 在toolbar不同功能
 * 在于每个Fragment中的的Adapter中的item项目操作不同
 */
public class UserInfoActivity
        extends BaseSwipeViewPagerActivity<BaseRecyclerHeadFragment>
        implements OnBoardFragmentInteractionListener<UserBoardItemBean>,
        OnPinsFragmentInteractionListener, OnEditDialogInteractionListener {
    private static final String TYPE_KEY = "TYPE_KEY";
    private static final String TYPE_TITLE = "TYPE_TITLE";

    @BindColor(R.color.white)
    int mColorTabIndicator;

    @BindString(R.string.url_image_small)
    String mFormatUrlSmall;
    @BindString(R.string.httpRoot)
    String mHttpRoot;
    @BindString(R.string.text_fans_attention)
    String mFansFollowingFormat;

    @Bind(R.id.toolbar_user)
    Toolbar mToolbar;
    @Bind(R.id.collapsingtoolbar_user)
    CollapsingToolbarLayout mCollapsingToolbar;
    @Bind(R.id.img_image_user)
    SimpleDraweeView mImageUser;
    @Bind(R.id.tv_user_name)
    TextView mTvUserName;
    @Bind(R.id.tv_user_location_job)
    TextView mTvUserLocationJob;
    @Bind(R.id.tv_user_friend)
    TextView mTvUserFriend;

    @Bind(R.id.tablayout_user)
    TabLayout mTabLayout;

    public String mKey;
    public String mTitle;
    public boolean isMe;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_my_user;
    }

    public static void launch(Activity activity, String key, String title) {
        Intent intent = new Intent(activity, UserInfoActivity.class);
        intent.putExtra(TYPE_TITLE, title);
        intent.putExtra(TYPE_KEY, key);
        activity.startActivity(intent);
    }

    @Override
    protected String getTAG() {
        return this.toString();
    }

    @Override
    protected String[] getTitleList() {
        return getResources().getStringArray(R.array.title_user_info);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mCollapsingToolbar.setExpandedTitleColor(Color.TRANSPARENT);//设置折叠后的文字颜色

        if (isMe) {
            addSubscription(getMyBoardListInfo());
        }
    }

    @Override
    protected ArrayList<BaseRecyclerHeadFragment> initFragmentList() {
        ArrayList<BaseRecyclerHeadFragment> fragments = new ArrayList<>(3);
        fragments.add(UserBoardFragment.newInstance(mKey));
        fragments.add(UserPinsFragment.newInstance(mKey));
        fragments.add(UserLikeFragment.newInstance(mKey));
        return fragments;
    }

    @Override
    protected void setupTabLayoutWithViewPager(ViewPager mViewPager) {
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setSelectedTabIndicatorColor(mColorTabIndicator);
    }


    @Override
    protected void getObligatoryData() {
        super.getObligatoryData();
        mTitle = getIntent().getStringExtra(TYPE_TITLE);
        mKey = getIntent().getStringExtra(TYPE_KEY);

        String userId = (String) SPUtils.get(mContext, Constant.USERID, "");
        Logger.d("is me " + mKey.equals(userId));
        isMe = mKey.equals(userId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addSubscription(getHttpsUserInfo());

    }


    private Subscription getMyBoardListInfo() {
        return RetrofitService.createAvatarService()
                .httpsBoardListInfo(mAuthorization, Constant.OPERATEBOARDEXTRA)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BoardListInfoBean>() {
                    @Override
                    public void onCompleted() {
                        Logger.d();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.d(e.toString());
                    }

                    @Override
                    public void onNext(BoardListInfoBean boardListInfoBean) {
                        Logger.d(boardListInfoBean.getBoards().size() + " ");


                    }
                });
    }

    //联网获取用户信息
    private Subscription getHttpsUserInfo() {
        return RetrofitService.createAvatarService()
                .httpsUserInfoRx(mAuthorization, mKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserMeAndOtherBean>() {
                    @Override
                    public void onCompleted() {
                        Logger.d();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.d(e.toString());
                    }

                    @Override
                    public void onNext(UserMeAndOtherBean userInfoBean) {
                        setUserData(userInfoBean);
                    }
                });


    }

    private void setUserData(UserMeAndOtherBean bean) {
        String url = bean.getAvatar();
        if (!TextUtils.isEmpty(url)) {
            if (!url.contains(mHttpRoot)) {
                url = String.format(mFormatUrlSmall, url);
            }
            new ImageLoadFresco.LoadImageFrescoBuilder(getApplicationContext(), mImageUser, url)
                    .setIsCircle(true, true)
                    .build();
//            mImageUser.setImageURI(Uri.parse(url));
        }
        String name = bean.getUsername();
        if (!TextUtils.isEmpty(name)) {
            mTvUserName.setText(name);
        } else {
            mTvUserName.setText("用户名为空");
        }

        String location = bean.getProfile().getLocation();
        String job = bean.getProfile().getJob();
        StringBuffer buffer = new StringBuffer();
        if (!TextUtils.isEmpty(location)) {
            buffer.append(location);
            buffer.append(" ");
        }
        if (!TextUtils.isEmpty(job)) {
            buffer.append(job);
        }
        if (!TextUtils.isEmpty(buffer)) {
            mTvUserLocationJob.setText(buffer);
        }

        mTvUserFriend.setText(String.format(mFansFollowingFormat, bean.getFollower_count(), bean.getFollowing_count()));

    }


    @Override
    public void onClickBoardItemImage(UserBoardItemBean bean, View view) {
        String boardId = String.valueOf(bean.getBoard_id());
        BoardDetailActivity.launch(this, boardId, bean.getTitle());
    }

    @Override
    public void onClickBoardItemOperate(UserBoardItemBean bean, View view) {
        Logger.d();
        if (isMe) {
            BoardEditDialogFragment fragment = BoardEditDialogFragment.create(String.valueOf(bean.getBoard_id()),
                    bean.getTitle(), bean.getDescription(), bean.getCategory_id());
            fragment.show(getSupportFragmentManager(), null);
        } else {
            Logger.d();
        }
    }

    @Override
    public void onClickPinsItemImage(PinsMainEntity bean, View view) {
        ImageDetailActivity.launch(this);
    }

    @Override
    public void onClickPinsItemText(PinsMainEntity bean, View view) {
        ImageDetailActivity.launch(this);
    }

    private Action1<UserBoardSingleBean> getNextAction() {
        return userBoardSingleBean -> {
            Logger.d();
            setSwipeRefresh();
        };
    }

    private Action1<Throwable> getErrorAction() {
        return throwable -> {
            NetUtils.checkHttpException(mContext, throwable, mSwipeRefresh);
            Logger.d(throwable.toString());
        };
    }

    @Override
    public void onDialogPositiveClick(String boardId, String name, String describe, String selectType) {
        Logger.d("name=" + name + " describe=" + describe + " selectPosition=" + selectType);

        RetrofitService.createAvatarService()
                .httpsEditBoard(mAuthorization, boardId, name, describe, selectType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getNextAction(), getErrorAction());
    }


    @Override
    public void onDialogNeutralClick(String boardId, String boardTitle) {
        Logger.d();

        DialogUtil.showDeleteDialog(mContext, boardTitle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startDeleteBoard(boardId);
            }
        });
    }

    private void startDeleteBoard(String boardId) {

        RetrofitService.createAvatarService()
                .httpsDeleteBoard(mAuthorization, boardId, Constant.OPERATEDELETEBOARD)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getNextAction(), getErrorAction());
    }
}