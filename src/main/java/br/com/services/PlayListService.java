package br.com.services;

import java.util.List;
import java.util.Optional;

import br.com.interfaces.model.IMusica;
import br.com.interfaces.model.IPlaylist;
import br.com.interfaces.model.IUsuario;
import br.com.interfaces.repository.IUsuarioRepository;
import br.com.interfaces.services.IArtistaService;
import br.com.interfaces.services.IPlayListService;
import br.com.interfaces.services.IRecomendacaoService;
import br.com.interfaces.services.IReproducaoService;
import br.com.model.Playlist;
import br.com.repositories.ArtistaRepository;
import br.com.repositories.MusicaRepository;
import br.com.repositories.UsuarioRepository;
import java.util.ArrayList;

public class PlayListService implements IPlayListService {
    
    private IRecomendacaoService recomendacaoService;
    private IUsuarioRepository usuarioRepository;
    private static final Integer MAX_COLABORADORES = 5;
    
    public PlayListService(IRecomendacaoService recomendacaoService) {
        usuarioRepository = UsuarioRepository.getUsuarioRepository();
        this.recomendacaoService = recomendacaoService;
    }

    @Override
    public Optional<IPlaylist> criarPlayList( String nome, IUsuario criador ) {
        
        if( nome == null || nome.isEmpty() ) {
            return Optional.empty();
        }
        
        if( !criador.getPermissaoCriarPlaylists() ) {
            return Optional.empty();
        }

        IPlaylist playlist = new Playlist( nome, criador );
        playlist.adicionarColaborador( criador );

        return Optional.of( playlist );
    }

    @Override
    public Integer compartilharPlayList( IPlaylist playlist, IUsuario usuario ) {
        
        if( usuarioRepository.findByNome(usuario.getNome()).isEmpty() ){
            return -1;
        }
        else if( !playlist.isPublica() && !playlist.isCompartilhavel() ) {
            return -2;
        }
        
        playlist.adicionarUsuario(usuario);
        return 0;
    }

    @Override
    public Integer convidarColaborador( IPlaylist playlist, IUsuario usuario ) {
        
        if( playlist.getColaboradores().contains( usuario ) ) {
            return -1;
        }
        
        if( playlist.getColaboradores().size() >= MAX_COLABORADORES ) {
            return -2;
        }
        
        playlist.adicionarColaborador( usuario );
        return 0;
    }

    @Override
    public List<IMusica> recomendarMusicasParaPlayList( IPlaylist playlist ) {
        
        List<IMusica> resultado = new ArrayList<>();
        
        try{
            resultado = recomendacaoService.recomendarMusicasParaPlayList( playlist );

            if(resultado == null || resultado.isEmpty()){
                return new ArrayList<>();
            }

            return resultado;
        }
        catch (Exception e){
            return resultado;
        }
    }

    @Override
    public Integer iniciarReproducaoPlayList(
            IPlaylist playlist,
            IUsuario usuario,
            IReproducaoService reproducaoService ) {
        
        if( !usuario.getPermissaoReproduzirMusicas() ) {
            return -1;
        }
        
        if( playlist.getMusicas().isEmpty() ) {
            return -2;
        }
        
        IArtistaService aService = new ArtistaService(ArtistaRepository.getArtistaRepository(), MusicaRepository.getMusicaRepository());
        
        reproducaoService.reproduzirPlayList(playlist, usuario, aService);
        return 0;
    }
}
