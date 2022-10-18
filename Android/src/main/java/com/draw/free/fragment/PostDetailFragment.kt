package com.draw.free.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.draw.free.Global
import com.draw.free.PostPositionActivity
import com.draw.free.PostVideoAdapter
import com.draw.free.R
import com.draw.free.ar.ARActivity
import com.draw.free.ar.EditPostActivity
import com.draw.free.databinding.FragmentPostDetailBinding
import com.draw.free.dialog.CameraPermission
import com.draw.free.dialog.ConfirmDialog
import com.draw.free.dialog.SinglePostBottomFragmentDialog
import com.draw.free.interfaceaction.IOpenProfile
import com.draw.free.interfaceaction.ICallback
import com.draw.free.interfaceaction.ToNextWork
import com.draw.free.model.Nft
import com.draw.free.model.Post
import com.draw.free.network.BaseResponse
import com.draw.free.network.RetrofitClient
import com.draw.free.nft.NftMetadataActivity
import com.draw.free.util.CustomList
import com.draw.free.viewmodel.ConfirmDialogModel
import com.draw.free.viewmodel.PostDetailFragmentViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import org.json.JSONObject
import retrofit2.Retrofit
import timber.log.Timber
import java.util.*

class PostDetailFragment : BaseInnerFragment<FragmentPostDetailBinding>() {

    init {
        layoutId = R.layout.fragment_post_detail
    }

    lateinit var player: ExoPlayer
    lateinit var requestManager: RequestManager
    private lateinit var viewModel: PostDetailFragmentViewModel
    lateinit var targetPostId: String
    lateinit var postDetailAction: PostDetailAction
    lateinit var customPostList: CustomList<Post>
    var playPosition = -1
    lateinit var adapter: PostVideoAdapter

    var holderPre: PostVideoAdapter.VpVHPostVideo? = null
    var holderNow: PostVideoAdapter.VpVHPostVideo? = null

    var dataSourceFactory: DataSource.Factory? = null
    var start: Date? = null
    var end: Date? = null
    var mPosition = -1;

    lateinit var vp: ViewPager2

    private val handler = Handler(Looper.getMainLooper())

    enum class VolumeState {
        ON, OFF
    }

    lateinit var volumeState: VolumeState


    interface PostDetailAction {
        fun openProfile(accountId: String)
        fun openNft(nft: Nft)
        fun pop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        player = ExoPlayer.Builder(requireContext()).build()
        volumeState = VolumeState.ON
        player.volume = 1f
        player.repeatMode = ExoPlayer.REPEAT_MODE_ONE
        dataSourceFactory = DefaultHttpDataSource.Factory()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(PostDetailFragmentViewModel::class.java)


        // postEditDialog
        viewModel.openPostEdit = { post: Post ->
            val moreMenuDialog = SinglePostBottomFragmentDialog()
            moreMenuDialog.isMine = post.isMine

            moreMenuDialog.editPostActivity = {
                moreMenuDialog.dismiss()

                val intent = Intent(activity, EditPostActivity::class.java)
                intent.putExtra("post", post)
                startActivity(intent)
                postDetailAction.pop()

            }

            moreMenuDialog.removePost = {
                viewModel.removePost(post.id) {
                    moreMenuDialog.dismiss()
                    postDetailAction.pop()
                    customPostList.deleteItem(post)
                    Global.makeToast("삭제되었습니다.")

                }
            }
            
            moreMenuDialog.declarationPost = {
                RetrofitClient.getPostService().reportPost(post.id).enqueue(BaseResponse() {
                    if (it.isSuccessful && it.code() == 200) {
                        moreMenuDialog.dismiss()
                        postDetailAction.pop()
                        customPostList.deleteItem(post)
                        Global.makeToast("신고 처리가 접수되었습니다.")
                    }


                    return@BaseResponse false
                })
            }

            moreMenuDialog.declarationUser = {
                RetrofitClient.getUserService().reportUser(post.producer.producerId).enqueue(BaseResponse() {
                    if (it.isSuccessful && it.code() == 200) {
                        moreMenuDialog.dismiss()
                        postDetailAction.pop()
                        customPostList.deleteItem(post)
                        Global.makeToast("신고 처리가 접수되었습니다.")
                    }


                    return@BaseResponse false
                })
            }


            moreMenuDialog.show(childFragmentManager, "edit")

        }

        // 배치하기 화면으로 이동
        viewModel.startPlaceActivity = object : ICallback {
            override fun callback(message: String?) {
                if (!CameraPermission.hasCameraPermission()) {
                    val activity = activity

                    val dialogModelForCloseActivity =
                        ConfirmDialogModel("AR 배치하기를 위해서는 카메라 권한이 필요합니다")
                    dialogModelForCloseActivity.clickYes = object : ToNextWork {
                        override fun next() {
                            if (CameraPermission.shouldShowRequestPermissionRationale(activity!!)) {
                                CameraPermission.requestCameraPermission(activity);
                            } else {
                                CameraPermission.launchPermissionSettings(activity);
                            }
                        }
                    }

                    val dialogForClose = ConfirmDialog(requireContext(), dialogModelForCloseActivity);
                    dialogForClose.show()

                    return
                }

                handler.post {
                    val placeActivity = Intent(activity, ARActivity::class.java)
                    placeActivity.putExtra("id", "${message}.glb")
                    placeActivity.putExtra("postId", "$message")
                    placeActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(placeActivity)

                    postDetailAction.pop()
                }
            }
        }

        // nft 발행하기 화면으로 이동
        viewModel.startMintingActivity = object : ICallback {
            override fun callback(message: String?) {
                val json = JSONObject(message!!)

                handler.post {
                    val mintingActivity = Intent(activity, NftMetadataActivity::class.java)
                    mintingActivity.putExtra("postId", json.getString("postId"))
                    mintingActivity.putExtra("thumbnail", json.getString("thumbnail"))
                    startActivity(mintingActivity)
                    postDetailAction.pop()
                }
            }
        }

        viewModel.startPositionActivity = {
            handler.post {
                val mintingActivity = Intent(activity, PostPositionActivity::class.java)
                mintingActivity.putExtra("post", it)
                startActivity(mintingActivity)
                postDetailAction.pop()
            }
        }



        vp = binding.vpPosts
        vp.visibility = View.VISIBLE

        Timber.d("$mPosition : __마지막 위치__")
        mPosition = customPostList.findPositionByKey(targetPostId)

        requestManager = Glide.with(this)
        adapter = PostVideoAdapter(customPostList, viewModel, childFragmentManager, requestManager)
        adapter.openNft = { postId ->
            viewModel.getNftByPostId(postId) { nft ->
                postDetailAction.pop()
                postDetailAction.openNft(nft)
            }
        }

        PostVideoAdapter.openProfile = object : IOpenProfile {
            override fun open(postId: String) {
                postDetailAction.openProfile(postId)
            }
        }

        vp.adapter = adapter
        vp.doOnPreDraw {
            vp.setCurrentItem(mPosition, false)
            Timber.d("이동 완료 : ${vp.currentItem}")
            mPosition = -1;
        }


        vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                targetPostId = customPostList.getDataByPosition(position).id

                // 이전 영상 Stop
                if (playPosition != position) {
                    holderPre = (vp.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(
                        playPosition
                    ) as PostVideoAdapter.VpVHPostVideo?
                    if (holderPre != null && !holderPre?.status.equals("freeze")) {
                        holderPre!!.thumbnail.visibility = View.VISIBLE
                        holderPre!!.playerView.player = null
                        holderPre!!.parent.setOnClickListener(null)
                        Timber.d("중지: $playPosition")
                        playPosition = -1
                    }
                }

                // 현재 영상 Start
                // 현재 영상 play
                binding.vpPosts.post {
                    holderNow =
                        (vp.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(position) as PostVideoAdapter.VpVHPostVideo?
                    if (holderNow != null && !holderPre?.status.equals("freeze")) {
                        holderNow!!.playerView.player = player
                        holderNow!!.parent.setOnClickListener { v -> toggleVolume(holderNow!!.volumeControl) }
                        val videoUrl: String = adapter.mData.get(vp.getCurrentItem()).video
                        val hlsMediaSource: HlsMediaSource =
                            HlsMediaSource.Factory(dataSourceFactory!!)
                                .setAllowChunklessPreparation(true)
                                .createMediaSource(MediaItem.fromUri(Uri.parse(videoUrl)))
                        player.setMediaSource(hlsMediaSource)
                        start = Date() // 시작 시각
                        player.prepare()
                        playPosition = position

                        //<editor-fold desc="페이징 처리">
                        val updateLine = (vp.adapter?.itemCount ?: 0) - 2
                        Timber.d("현재 위치 : $position, 업데이트 시점 길이 : $updateLine")
                        if (position >= updateLine) {
                            customPostList.getNextData()
                        }
                        //</editor-fold>
                    }
                }
            }
        })


        customPostList.mLiveData.observe(viewLifecycleOwner) {
            adapter.updateItem(it)
            adapter.notifyDataSetChanged()
        }


        viewModel.likeResponse.observe(viewLifecycleOwner) {
            if (it.equals("Like")) {
                Global.makeToast("좋아요를 누르셨습니다.")
            } else if (it.equals("UnLike")) {
                Global.makeToast("좋아요를 취소하셨습니다.")
            }
        }


        //<editor-fold desc="임시-추가">
        // player 리스너는 viewPager 콜백 안에 넣지말고 밖으로 빼야함
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == ExoPlayer.STATE_READY) {
                    Timber.d("준비됨 : $playPosition")
                    player.play()
                    end = Date()
                    Timber.d("걸린시간(ms):  ${(end!!.time - start!!.time)}")
                    holderNow!!.thumbnail.visibility = View.INVISIBLE
                }
            }
        })

        //</editor-fold>


    }

    override fun onStart() {
        super.onStart()
        if (!isHidden) {
            showBottomNav.show(false)
            setPreviousButton.set(isSet = true, isLight = true)
        }

        if (holderNow != null && !holderPre?.status.equals("freeze")) {
            holderNow!!.playerView.player = player
            holderNow!!.parent.setOnClickListener { v -> toggleVolume(holderNow!!.volumeControl) }
            val videoUrl: String = adapter.mData.get(vp.getCurrentItem()).video
            val hlsMediaSource: HlsMediaSource =
                HlsMediaSource.Factory(dataSourceFactory!!).setAllowChunklessPreparation(true)
                    .createMediaSource(MediaItem.fromUri(Uri.parse(videoUrl)))
            player.setMediaSource(hlsMediaSource)
            start = Date() // 시작 시각
            player.prepare()

            //<editor-fold desc="페이징 처리">
        }
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onStop() {
        super.onStop()
        player.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
        Timber.d("플레이어 정리함")
    }

    private fun toggleVolume(volumeControl: ImageView) {
        if (volumeState == VolumeState.OFF) {
            setVolumeControl(VolumeState.ON, volumeControl)
        } else if (volumeState == VolumeState.ON) {
            setVolumeControl(VolumeState.OFF, volumeControl)
        }
    }

    private fun setVolumeControl(state: VolumeState, volumeControl: ImageView) {
        volumeState = state
        if (state == VolumeState.OFF) {
            player.volume = 0f
            animateVolumeControl(volumeControl)
        } else if (state == VolumeState.ON) {
            player.volume = 1f
            animateVolumeControl(volumeControl)
        }
    }

    private fun animateVolumeControl(volumeControl: ImageView?) {
        if (volumeControl != null) {
            volumeControl.bringToFront()
            if (volumeState == VolumeState.OFF) {
                requestManager.load(R.drawable.ic_volume_off_grey_24dp).into(volumeControl)
            } else if (volumeState == VolumeState.ON) {
                requestManager.load(R.drawable.ic_volume_up_grey_24dp).into(volumeControl)
            }
            volumeControl.animate().cancel()
            volumeControl.alpha = 1f
            volumeControl.animate().alpha(0f).setDuration(600).startDelay = 1000
        }
    }
}