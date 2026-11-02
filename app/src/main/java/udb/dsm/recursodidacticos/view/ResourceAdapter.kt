package udb.dsm.recursodidacticos.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import udb.dsm.recursodidacticos.R
import udb.dsm.recursodidacticos.model.Recurso

class ResourceAdapter (

    private var listaRecursos: List<Recurso>,
    private val listener: OnItemClickListener
    ) : RecyclerView.Adapter<ResourceAdapter.ViewHolder>() {
        interface OnItemClickListener {
            fun onItemClick(recurso: Recurso)
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivRecurso: ImageView = view.findViewById(R.id.ivRecurso)
            val tvTitulo: TextView = view.findViewById(R.id.tvTitulo)
            val tvTipo: TextView = view.findViewById(R.id.tvTipo)
            val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
            val tvRating: TextView = view.findViewById(R.id.tvRating)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_resource, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val recurso = listaRecursos[position]
            holder.tvTitulo.text = recurso.titulo
            holder.tvTipo.text = "Tipo: ${recurso.tipo.uppercase()}"
            holder.tvDescripcion.text = recurso.descripcion
            holder.tvRating.text = "★ ${recurso.rating ?: 0.0}"

            // carga imagen con Picasso
            if (recurso.imagen.isNotEmpty()) {
                Picasso.get().load(recurso.imagen).placeholder(R.mipmap.ic_launcher).into(holder.ivRecurso)
            }

            // detectar clic en el elemento (para modificar/eliminar si es docente, o ver detalles si es estudiante)
            holder.itemView.setOnClickListener {
                listener.onItemClick(recurso)
            }
        }

        override fun getItemCount(): Int = listaRecursos.size

        fun actualizarLista(nuevaLista: List<Recurso>) {
            listaRecursos = nuevaLista
            notifyDataSetChanged()
        }
    }