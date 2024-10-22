package com.fixeam.tubesave.view

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fixeam.tubesave.R
import com.fixeam.tubesave.controller.downloadList
import com.fixeam.tubesave.controller.openYouTubeVideo
import com.fixeam.tubesave.databinding.DownloadItemBinding
import com.fixeam.tubesave.databinding.FragmentDownloadBinding
import com.fixeam.tubesave.utils.Calculate
import com.fixeam.tubesave.utils.Md5Utils
import com.fixeam.tubesave.utils.ScreenUtils
import com.flyjingfish.openimagelib.OpenImage
import com.flyjingfish.openimagelib.enums.MediaType
import java.io.File
import kotlin.math.floor

class DownloadFragment : Fragment() {
    private lateinit var binding: FragmentDownloadBinding
    private var adapter: DownloadListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDownloadBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initList() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.setItemViewCacheSize(3)
        binding.recyclerView.setHasFixedSize(true)
        adapter = DownloadListAdapter()
        binding.recyclerView.adapter = adapter
        adapter!!.notifyDataSetChanged()
        // 添加分割线
        val dividerItemDecoration = DividerItemDecoration(binding.recyclerView.context, LinearLayoutManager.VERTICAL)
        binding.recyclerView.addItemDecoration(dividerItemDecoration)
    }

    inner class DownloadListAdapter : RecyclerView.Adapter<DownloadItemHolder>() {
        override fun getItemCount(): Int {
            return downloadList.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadItemHolder {
            val binding = DownloadItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return DownloadItemHolder(binding)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: DownloadItemHolder, position: Int) {
            val item = downloadList[position]
            item.update = {
                notifyItemChanged(position)
            }

            // 检测文件是否被删除
            if (item.status == "deleted") {
                val spannableString = SpannableString(item.title)
                spannableString.setSpan(
                    StrikethroughSpan(), // 使用 StrikethroughSpan
                    0, // 起始位置
                    item.title.length, // 结束位置
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                holder.binding.title.text = spannableString
            } else {
                holder.binding.title.text = item.title
            }

            // 计算进度
            val totalSize = item.size + item.audioSize
            val percentage = item.percentage + item.audioPercentage
            // Log.i("ABLog", "${ Calculate.bytesToReadableSize(percentage) } / ${ Calculate.bytesToReadableSize(totalSize) }, ${ Calculate.bytesToReadableSize(item.speed) } / s")

            // 更新进度显示
            holder.binding.totalSize.text = if (item.status == "done" || item.status == "deleted") {
                ""
            } else {
                "${ Calculate.bytesToReadableSize(percentage) } / ${ Calculate.bytesToReadableSize(totalSize) }"
            }
            val ratio = floor(percentage.toDouble() / totalSize * 10000) / 100
            holder.binding.progressBar.setProgress(ratio.toInt())
            holder.binding.ratio.text = if (item.status == "done") {
                Calculate.bytesToReadableSize(totalSize)
            } else if (item.status == "deleted") {
                ""
            } else {
                "${ ratio }%"
            }
            holder.binding.message.text = item.msg
            holder.binding.speed.text = "${ Calculate.bytesToReadableSize(item.speed) } / s"

            // 更新状态显示
            holder.binding.download.visibility = View.GONE
            holder.binding.pause.visibility = View.GONE
            holder.binding.reset.visibility = View.GONE
            holder.binding.speed.visibility = View.GONE
            holder.binding.progressBar.visibility = View.GONE
            if (item.status == "fail" || item.status == "deleted") holder.binding.reset.visibility = View.VISIBLE
            if (item.status == "pause") holder.binding.download.visibility = View.VISIBLE
            if (item.status == "downloading") {
                holder.binding.speed.visibility = View.VISIBLE
                holder.binding.pause.visibility = View.VISIBLE
                holder.binding.progressBar.visibility = View.VISIBLE
            }

            // 插入图片
            Glide.with(requireContext())
                .load(item.thumbnail.url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.binding.coverView)

            // 绑定按钮事件
            holder.binding.download.setOnClickListener {
                item.downloadManager?.resume()
                item.status = "downloading"
                item.msg = "下载中"
                item.update!!()
            }
            holder.binding.pause.setOnClickListener {
                item.downloadManager?.pause()
                item.status = "pause"
                item.msg = "暂停中"
                item.update!!()
            }
            holder.binding.reset.setOnClickListener {
                item.percentage = 0
                item.audioPercentage = 0
                item.status = "wait"
                item.msg = "重试中"
                item.update!!()
            }

            // 删除事件
            fun delete(file: Boolean = false) {
                item.downloadManager?.cancel()
                downloadList.removeAt(position)
                adapter?.notifyItemRemoved(position)

                if (file) {
                    val downloadFolder = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS)
                    val video = File(downloadFolder, Md5Utils.generateMD5String(item.url) + ".video")
                    val audio = File(downloadFolder, Md5Utils.generateMD5String(item.audioUrl) + ".audio")
                    video.delete()
                    audio.delete()
                    if (!item.savePath.isNullOrBlank()) {
                        File(item.savePath!!).delete()
                    }
                    ScreenUtils(requireContext()).showToast("删除成功")
                }
            }

            // 绑定项目事件
            holder.binding.coverView.setOnClickListener {
                OpenImage.with(activity)
                    .setNoneClickView()
                    .setImageUrlList(downloadList.map { it.thumbnail.url }, MediaType.IMAGE)
                    .setClickPosition(position)
                    .setOnItemLongClickListener { baseFragment, openImageUrl, _ ->
                        ImageFlashWindow(
                            requireActivity() as AppCompatActivity,
                            baseFragment.requireView(),
                            openImageUrl.imageUrl,
                            false
                        )
                    }
                    .show()
            }
            holder.binding.coverView.setOnLongClickListener {
                ImageFlashWindow(
                    requireActivity() as AppCompatActivity,
                    binding.root,
                    item.thumbnail.url
                )
                false
            }
            holder.itemView.setOnClickListener {
                if (!item.savePath.isNullOrBlank()) {
                    val file = File(item.savePath!!)
                    if (file.exists()) {
                        // 使用 FileProvider
                        val videoUri: Uri = Uri.parse(item.savePath!!)

                        // 创建 Intent
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(videoUri, "video/*") // 设置 MIME 类型
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // 授予读取权限
                        }

                        // 启动 Activity
                        requireContext().startActivity(intent)
                    } else {
                        // 文件不存在的处理
                        // 你可以显示一个 Toast 或其他提示
                        ScreenUtils(requireContext()).showToast("文件已经被删除！")
                    }
                }
            }
            holder.itemView.setOnLongClickListener {
                DownloadOption(
                    requireActivity() as AppCompatActivity,
                    binding.root, {
                        openYouTubeVideo(requireContext(), item.id)
                    },{
                        delete()
                    },{
                        delete(true)
                    }
                )
                true
            }
        }
    }

    class DownloadItemHolder(val binding: DownloadItemBinding) : RecyclerView.ViewHolder(binding.root)
}