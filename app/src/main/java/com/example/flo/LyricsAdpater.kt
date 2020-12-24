import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flo.viewmodel.Lyric
import com.example.flo.databinding.RowBinding

class LyricsAdapter (private val context : Context, private val data:List<Lyric>) : RecyclerView.Adapter<LyricsAdapter.LyricsVH>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LyricsVH {
        val binding = RowBinding.inflate(
            LayoutInflater.from(context), parent, false
        )

        return LyricsVH(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: LyricsVH, position: Int) {
        holder.onBind(data[position])
    }

    class LyricsVH(private val binding: RowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(data: Lyric) {
            binding.lyric = data
        }


    }
}